/**
 * Copyright 2016 Yuan Yao
 * University of Nottingham
 * Email: yvy@cs.nott.ac.uk (yuanyao1990yy@icloud.com)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details 
 *  <http://www.gnu.org/licenses/gpl-3.0.html>.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uno.gpt.generator;

import java.util.ArrayList;
import java.util.Random;

import uno.gpt.nodes.*;

/**
 * @version 1.0
 */
public class Generator
{
	
	/** random */
	private static Random rm = new Random(100);
	
	/** total number of goals in this goal plan tree */
	private int totalGoals = 0;
	
	/** total number of plans in this goal plan tree */
	private int totalPlans = 0;
	
	/** total number of actions in this goal plan tree */
	private int totalActions = 0;
		
	/** number environment variables */
	private int num_var;
	
	/** number of goals */
	private int num_goal;
	
	/** number of plans */
	private int num_plan;
	
	/** number of actions */
	private int num_action;
	
	/** depth of the tree */
	private int depth;
	
	/** probability of actions and subgoals executed in parallel */
	private double p_paralle;
	
	/** id of the tree*/
	private int id;
	
	public Generator(int depth, int num_goal, int num_plan, int num_action, double prob_parallel,
			int num_var)
	{
		this.depth = depth;
		this.num_goal = num_goal;
		this.num_plan = num_plan;
		this.num_action = num_action;
		this.p_paralle = prob_parallel;
		this.num_var = num_var;

	}
	
	/** construct return the top-level goal*/
	public GoalNode genTopLevelGoal(int id)
	{
		this.id = id;
		// the list of variables that are available to be the precondition
		ArrayList<Integer> var_pre = new ArrayList<Integer>();
		// the list of variables that are available to be the postcondition
		ArrayList<Integer> var_post = new ArrayList<Integer>();
		// initially all variables are available
		for(int i = 1; i < this.num_var+1; i++)
		{
			var_post.add(i);
			var_post.add(-i);
		}	
				
		// create the top-level goal
		ArrayList<PlanNode> plans = new ArrayList<PlanNode>();
		ArrayList<Literal> inc = new ArrayList<Literal>();
		GoalNode rootnode = new GoalNode("T" + this.id + "-G" + String.valueOf(this.totalGoals++), plans, inc);
		// start constructing the goal by adding its associated plans, given its current depth and
		// the set of available variables
		constructGoal(rootnode, 0, var_pre, var_post);
		
		return rootnode;
	}
	
	/** 
	 * add plans to the target goal 
	 * @param goal The target goal
	 * @param currentdepth The current depth of the target goal
	 * @param var_pre The set of available preconditions
	 * @param var_post The set of available postconditions
	 * */
	public ArrayList<Integer> constructGoal(GoalNode gl, int currentdepth, ArrayList<Integer> var_pre, 
			ArrayList<Integer> var_post)
	{	
		// the list of plans that are used to achieve this goal
		ArrayList<PlanNode> plans = gl.getPlans();
		// the list of available preconditions after achieving this goal
		ArrayList<Integer> result_pre = new ArrayList<Integer>();
		
		// if there is no conditions made true by previous steps (i.e., no available preconditions)
		// the precondition of the plans will be c1, ~c1, c1, ~c1, .... where c1 is chosen randomly
		// from the set of environment variables
		int index = 0;
		boolean ef = true;
		if(var_pre.size() == 0)
		{
			index = var_post.get(rm.nextInt(var_post.size()));
			ef = index > 0 ? true : false;
		}
		
		// add plans
		for(int i = 0; i < this.num_plan; i++)
		{	
			// precondition of this plan
			ArrayList<Literal> planpre = new ArrayList<Literal>();	
			// if there is no conditions made true by previous steps (e.g., plans for the top-level goal)
			// then the precondition of the plans are c1, ~c1, c1, ~c1, ....
			if(var_pre.size() == 0)
			{
				planpre.add(new Literal(Math.abs(index)-1, i % 2 == 0 ? ef : !ef));
			}
			else
			{
				// if this plan is not used to achieve the top-level goal, then its precondition comes from the conditions
				// that are established by previous steps in this goal-plan tree
				// random select one as precondition of this plan
				int y = var_pre.get(rm.nextInt(var_pre.size()));
				planpre.add(new Literal(Math.abs(y)-1, y > 0 ? true : false));
			}
			
			// create the plan and add it to the list of associated plans of the target goal
			ArrayList<Node> pb = new ArrayList<Node>();
			ArrayList<Literal> inc = new ArrayList<Literal>();
			PlanNode pl = new PlanNode("T" + this.id +"-P" + String.valueOf(this.totalPlans++), pb, planpre, inc);
			plans.add(pl);
			
			// copy the list of available preconditions
			ArrayList<Integer> next_pre = new ArrayList<Integer>();
			for(int k = 0; k < var_pre.size(); k++)
			{
				next_pre.add(var_pre.get(k));
			}
			
			// copy the list of available postconditions
			ArrayList<Integer> next_post = new ArrayList<Integer>();
			for(int k = 0; k < var_post.size(); k++)
			{
				next_post.add(var_post.get(k));
			}
			
			// decide if the actions and plans in this plan are executed in parallel
			boolean is_parallel = rm.nextDouble() <= this.p_paralle;
			ArrayList<Integer> ax;
			// if they are executed in parallel and the number of available postcondition is enough for
			// them to execute in parallel
			if(is_parallel && var_post.size() > this.num_action+this.num_goal )
			{
				// construct the plan with all actions and subgoals executed in parallel
				ax = constructParallelPlan(pl, currentdepth + 1, next_pre, next_post);
			}else
			{
				// construct the plan with all actions and subgoals executed sequentially  
				ax = constructPlan(pl, currentdepth + 1, next_pre, next_post);
			}
			
			// summarise the postcondition and update the available precondition for later steps
			if(i == 0)
			{
				for(int k = 0; k < ax.size(); k++)
				{
					result_pre.add(ax.get(k));
				}
			}else
			{
				for(int k = result_pre.size()-1; k >= 0; k--)
				{
					if(!ax.contains(result_pre.get(k)))
							result_pre.remove(k);
				}
			}
		}
		return result_pre;
	}
		
	
	/** 
	 * construct plan with all its actions and subgoals executed sequentially 
	 * @param pl The target plan
	 * @param currentdepth The current depth of the tree
	 * @param var_pre The set of available preconditions
	 * @param var_post The set of available postconditions
	 */
	public ArrayList<Integer> constructPlan(PlanNode pl, int currentdepth, ArrayList<Integer> var_pre, 
			ArrayList<Integer> var_post)
	{		
		// decide the number of goals
		int numofgoals;
		if(currentdepth == this.depth)
			numofgoals = 0;
		else
			numofgoals = this.num_goal;
		
		// the set of available preconditions after executing this plan 
		ArrayList<Integer> result_pre = new ArrayList<Integer>();
		for(int k = 0; k < result_pre.size(); k++)
		{
			result_pre.add(var_pre.get(k));
		}
		
		// the set of available precondition for actions in this plan
		ArrayList<Integer> action_pre = new ArrayList<Integer>();
		action_pre.add(pl.getPre().get(0).getValue() ? (pl.getPre().get(0).getIndex()+1) : -(pl.getPre().get(0).getIndex()+1));
		
		// add actions to the plan
		for(int i = 0; i < this.num_action; i++)
		{		
			// generate precondition for this action 
			int pospre = action_pre.get(rm.nextInt(action_pre.size()));
			ArrayList<Literal> actpre = new ArrayList<Literal>();
			actpre.add( new Literal(Math.abs(pospre)-1, (pospre > 0) ? true : false));
			// generate its post-condition
			int index = var_post.get(rm.nextInt(var_post.size()));
			boolean value = index > 0 ? true : false; // positive or negative
			ArrayList<Literal> actpost = new ArrayList<Literal>();
			Literal e = new Literal(Math.abs(index)-1, value);
			actpost.add(e);
			// apply the changes to the set of available precondition
			if(!result_pre.contains(index)) result_pre.add(index);
			if(result_pre.contains(-index)) result_pre.remove(result_pre.indexOf(-index));
			if(!action_pre.contains(index)) action_pre.add(index);
			if(action_pre.contains(-index)) action_pre.remove(action_pre.indexOf(-index));
			ArrayList<Literal> inc = new ArrayList<Literal>();
			ActionNode act = new ActionNode("T" + this.id+ "-A" + String.valueOf(this.totalActions++), actpre, inc, actpost);
			// add this action to the planbody
			pl.getPlanBody().add(act);
		}
		
		// add subgoals to the plan
		for(int i = 0; i < numofgoals; i++)
		{
			// create the subgoal 
			ArrayList<PlanNode> pls = new ArrayList<PlanNode>();
			ArrayList<Literal> inc = new ArrayList<Literal>();
			GoalNode g = new GoalNode("T" + this.id + "-G" + String.valueOf(this.totalGoals++), pls, inc);
			pl.getPlanBody().add(g);
			// construct this subgoal and update the set of available preconditions
			result_pre = constructGoal(g, currentdepth, result_pre, var_post);
		}
		return result_pre;
	}

	
	/** 
	 * construct plan with all its actions and subgoals executed in parallel 
	 * @param pl The target plan
	 * @param currentdepth The current depth of the tree
	 * @param var_pre The set of available preconditions
	 * @param var_post The set of available postconditions
	 * */
	public ArrayList<Integer> constructParallelPlan(PlanNode pl, int currentdepth, ArrayList<Integer> var_pre, 
			ArrayList<Integer> var_post)
	{	
		// decide the number of goals
		int numofgoals;
		if(currentdepth == this.depth)
			numofgoals = 0;
		else
			numofgoals = this.num_goal;	
		
		// the set of available preconditions after executing this plan 
		ArrayList<Integer> result_pre = new ArrayList<Integer>();
		for(int i = 0; i < var_pre.size(); i++)
		{
			result_pre.add(var_pre.get(i));
		}
		// the set of available variables
		ArrayList<Integer> ava_var = new ArrayList<Integer>();
		for(int i = 0; i < var_post.size(); i++)
		{
			ava_var.add(var_post.get(i));
		}
		
		// create all parallel steps
		ArrayList<Node> p_composition = new ArrayList<Node>();
		// create all actions which are executed in parallel
		for(int i = 0; i < this.num_action; i++)
		{
			// precondition is the same as the precondition of the plan
			// generate its post-condition
			int index = ava_var.get(rm.nextInt(ava_var.size()));
			boolean value = index > 0 ? true : false; // positive or negative
			ArrayList<Literal> actpost = new ArrayList<Literal>();
			Literal e = new Literal(Math.abs(index)-1, value);
			actpost.add(e);			
			// apply the changes to available precondition set which are caused by the postcondition of this action
			if(!result_pre.contains(index)) result_pre.add(index);
			if(result_pre.contains(-index)) result_pre.remove(result_pre.indexOf(-index));
			// and this condition (and its negation) cannot be used for other actions or subgoals
			ava_var.remove(ava_var.indexOf(index));
			ava_var.remove(ava_var.indexOf(-index));
			// create the action
			ArrayList<Literal> inc = new ArrayList<Literal>();
			ActionNode act = new ActionNode("T" + this.id+ "-A" + String.valueOf(this.totalActions++), pl.getPre(), inc, actpost);
			// add this action to the parallel composition
			p_composition.add(act);
		}
		
		// create all subgoals that are executed in parallel
		int numofvar = numofgoals != 0 ? ava_var.size() / (2*numofgoals) : -1; // calculate the number of variables for each subgoal
		for(int i = 0; i < numofgoals; i++)
		{
			// create the subgoal
			ArrayList<PlanNode> pls = new ArrayList<PlanNode>();
			ArrayList<Literal> inc = new ArrayList<Literal>();
			GoalNode g = new GoalNode("T" + this.id + "-G" + String.valueOf(this.totalGoals++), pls, inc);
			p_composition.add(g);
			// precondition of the plans are the same other parallel executed actions
			int pre_var = pl.getPre().get(0).getValue() ? (pl.getPre().get(0).getIndex()+1) : -(pl.getPre().get(0).getIndex()+1);
			ArrayList<Integer> next_pre = new ArrayList<Integer>();
			next_pre.add(pre_var);
			// pick variables for this subgoal
			ArrayList<Integer> next_post = new ArrayList<Integer>();
			while(next_post.size() < numofvar)
			{
				int x = ava_var.get(rm.nextInt(ava_var.size()));
				next_post.add(x);
				next_post.add(-x);
				ava_var.remove(ava_var.indexOf(x));
				ava_var.remove(ava_var.indexOf(-x));
			}
			// construct the goal and update the set of available preconditions
			ArrayList<Integer> n_post = constructGoal(g, currentdepth, next_pre, next_post);
			
			// combine the effects of this subgoal with previous actions
			for(int k = 0; k < n_post.size(); k++)
			{
				if(!result_pre.contains(n_post.get(k)))
				{
					result_pre.add(n_post.get(k));
				}
				if(result_pre.contains(-n_post.get(k)))
				{
					result_pre.remove(result_pre.indexOf(-n_post.get(k)));
				}
			}
		}
		// create the parallel composition and add it to the planbody
		ParallelNode pn = new ParallelNode("||", p_composition);
		pl.getPlanBody().add(pn);
		
		return result_pre;
	}
}
