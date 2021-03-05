/*
 * Copyright 2016 Yuan Yao
 * University of Nottingham
 * Email: yvy@cs.nott.ac.uk (yuanyao1990yy@icloud.com)
 *
 * Modified 2019 IPC Committee
 * Contact: https://www.intentionprogression.org/contact/
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

package uno.gpt.generators;
import uno.gpt.structure.*;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.*;

/**
 * @version 2.0
 * @author yuanyao
 *
 * A new version of goal-plan tree generator
 */
public class SynthGenerator extends AbstractGenerator {
	/** Default values */
	static final int def_depth = 3,
						def_num_goal = 3,
						def_num_plan = 3,
						def_num_action = 3,
						def_num_var = 60,
						def_num_selected = 30;

	static final double def_prob_leaf = 0d;

	/** id of the tree */
	private int id;

	/** total number of goals in this goal plan tree */
	private int treeGoalCount;

	/** total number of plans in this goal plan tree */
	private int treePlanCount;

	/** total number of actions in this goal plan tree */
	private int treeActionCount;

	/** random generators */
	final private Random rm;

	/** depth of the tree */
	final private int tree_depth;

	/** number of trees */
	final private int num_tree;

	/** number of goals */
	final private int num_goal;

	/** number of plans */
	final private int num_plan;

	/** number of actions */
	final private int num_action;

	/** number of environment variables */
	final private int num_var;

	/** number of environment variables that can be used as post-condition of actions */
	final private int num_sel;

	/** probabilty of a plan being leaf plan */
	final private double prob;

	/** the set of variables selected*/
	private ArrayList<Integer> selected_indexes;

	/** the set of irrelevant literals*/
	private ArrayList<Literal> is;

	/** Constructor add a new variable num_sel */
	SynthGenerator(int seed, int tree_depth, int num_tree, int num_goal, int num_plan, int num_action, int num_var,
				   int num_sel, double prob) {
		this.rm = new Random(seed);
		this.tree_depth = tree_depth;
		this.num_tree = num_tree;
		this.num_goal = num_goal;
		this.num_plan = num_plan;
		this.num_action = num_action;
		this.num_var = num_var;
		this.num_sel = num_sel;
		this.prob = prob;
	}

	/**
	 * Generate environment
	 * @return the generated environment*/
	public HashMap<String, Literal> genEnvironment(){
		environment = new HashMap<>();
		Literal workingLit;

		// generate goal literals, all of which are false initially
		for (int i = 0; i < num_tree; i++) {
			workingLit = new Literal("G-" + i, false);
			environment.put(workingLit.getId(), workingLit);
		}
		// generate all the  environment literals with their initial value
		for (int i = 0; i < num_var; i++) {
			boolean v = rm.nextBoolean();
			workingLit = new Literal("EV-" + i, v);
			environment.put(workingLit.getId(), workingLit);
		}
		return environment;
	}

	/**
	 * A function for producing the top level goals for the GPTs
	 * @param index The index of the Goal being produced
	 * @return A Goal Node
	 */
	@Override
	public GoalNode genTopLevelGoal(int index) {
		// Set the generator id
		this.id = index;
		// Set the counters for this tree to 0
		this.treeGoalCount = 0;
		this.treePlanCount = 0;
		this.treeActionCount = 0;

		// randomly select the conditions that can be the post-condition of action in this gpt, i.e. the set es
		ArrayList<Literal> selected = selectVar(this.num_sel);
		ArrayList<Literal> actL = new ArrayList<>(selected.subList(0,this.num_sel));
		for(int i = 0; i < this.num_sel; i++){
			Literal cu = actL.get(i).clone();
			cu.setState(!actL.get(i).getState());
			actL.add(cu);
		}

		// get the irrelevant literals, i.e. the set vs/es
		this.is = new ArrayList<>(selected.subList(this.num_sel,selected.size()));
		// the goal-condition
		ArrayList<Literal> gcs = new ArrayList<>();
		// add the goal condition
		gcs.add(new Literal("G-" + index, true));
		// create the top-level goal
		GoalNode tpg = createGoal(0, actL, new ArrayList<>(), gcs);
		// return top-level goal
		return tpg;
	}


	/**
	 * select m literals that can be used as post-condition of actions in the gpt
	 * @return a new list where the first m literals are selected, and the others are irrelevant literals
	 */
	private ArrayList<Literal> selectVar(int m){
		// note that m must be less than or equal to num_var
		// randomly pick m different variables
		this.selected_indexes = new ArrayList<>();
		while (selected_indexes.size() < m){
			int index = rm.nextInt(this.num_var);
			if(!selected_indexes.contains(index)){
				selected_indexes.add(index);
			}
		}
		// return the corresponding literal in the current environment
		ArrayList<Literal> result = new ArrayList<>();
		// the set of literals that are not selected
		ArrayList<Literal> irr = new ArrayList<>();

		for(int i = 0; i < this.num_var; i++){
			// if the index of this literal is selected
			if(this.selected_indexes.contains(i)){
				result.add(environment.get("EV-" + i));
			}
			// otherwise, this literal is categorised as irrelevant
			else {
				irr.add(environment.get("EV-" + i));
			}

		}
		result.addAll(irr);
		return  result;
	}


	/**
	 * a function to recursively create and construct a goal and all its hierarchies below
	 * @param depth current depth
	 * @param as the set of literals could be used as postcondition of actions in this tree
	 * @param ps the precondition of this goal, i.e. common condition fro all plans to achieve this goal
	 * @param gcs the goal-condition of this goal
	 * @return
	 */
	private GoalNode createGoal(int depth, ArrayList<Literal> as, ArrayList<Literal> ps, ArrayList<Literal> gcs){

		// create the goal node
		GoalNode goalNode = new GoalNode("T" + this.id + "-G" + this.treeGoalCount++);
		// generate all plans to achieve this goal
		ArrayList<PlanNode> plans = new ArrayList<>();
		// clone the irrelevant literals, we assume the number of literals in potential is greater than or equals to
		// the number of plans need to be generated, these conditions are treated as pure environment variables which
		// cannot be affected by the GPT itself (i.e., can be changed by the environment itself or other intentions)
		ArrayList<Literal> potential = (ArrayList<Literal>) is.clone();
		// create each plan one by one
		for(int i = 0; i < this.num_plan; i++){
			// generate its precondition (context condition), the p-effect part
			ArrayList<Literal> prec = new ArrayList<>(ps);
			// if there are pure environment conditions remains
			if(potential.size() > 0){
				if (prec.size() == 2){
					prec.remove(1);
				}
				// randomly select a pure environmental condition
				int j = rm.nextInt(potential.size());
				// add it to the precondtion of this plan
				prec.add(potential.get(j));
				// remove it from the set of possible environmental literals
				potential.remove(j);
			}

			// create the plan
			PlanNode plan;
			// each plan has l% chance to be a leaf plan
			if(rm.nextDouble() < this.prob){
				// if it is a leaf plan, its depth is set to the maximum
				plan = createPlan(this.tree_depth - 1, as, prec, gcs);
			}else {
				plan = createPlan(depth, as, prec, gcs);
			}
			// add it to the set of plans
			plans.add(plan);
		}
		// attach all plans to the goal
		goalNode.getPlans().addAll(plans);
		// add its goal-condition
		goalNode.getGoalConds().addAll(gcs);
		// return the goal node
		return goalNode;
	}


	/**
	 * a function to generate plans to achieve a particular goal
	 * @param depth the depth of this plan
	 * @param as the set of conditions that can be postcondition of actions
	 * @param prec the precondition of this context condition
	 * @param gcs the goal condition this plan is going to achieve
	 * @return the plan
	 */
	private PlanNode createPlan(int depth, ArrayList<Literal> as, ArrayList<Literal> prec, ArrayList<Literal> gcs){
		PlanNode planNode = new PlanNode("T" + this.id + "-P" + this.treePlanCount++);

		// initialise the plan body
		ArrayList<Node> planbody = new ArrayList<>();
		// the number of steps in a plan
		int stepnum;
		// if it is a leaf plan then it only contains actions
		if(depth == this.tree_depth - 1){
			stepnum = this.num_action;
		}
		// otherwise, it contains both actions and subgoals
		else{
			stepnum = this.num_action + this.num_goal;
		}

		// initialise the planbody, we assume they are all actions at first
		ArrayList<ActionNode> steps = new ArrayList<>();

		// create the list of execution steps (actions) based on p-effect rules, and return the resulting post-condition
		ArrayList<Literal> postc = createPlanBody(stepnum, prec, gcs, as, steps);
		// assign type for each step, i.e., in fact not all steps are actions
		ArrayList<Boolean> types = assignPosition(stepnum);
		// calculate the safe conditions for subgoals
		ArrayList<Literal> safeC = safeCondition(steps, as);

		// create each action and subgoal
		for(int i = 0; i < types.size(); i++){
			// if it is an action
			if(types.get(i)){
				// create the action
				ActionNode actionNode = new ActionNode("T" + this.id + "-A" + this.treeActionCount++, steps.get(i).getPreC(),
						steps.get(i).getPostC());
				// and add it to the plan body
				planbody.add(actionNode);
			}
			// if it is a subgoal
			else{
			    // remove the goal-condition of a subgoal from the plan's postcondition
                ArrayList<Literal> pc = steps.get(i).getPostC();
                for(int m = 0; m < pc.size(); m++){
                    for(int n = 0; n < postc.size(); n++){
                        if(postc.get(n).getId().equals(pc.get(m).getId()) &&
                        postc.get(n).getState() == pc.get(m).getState()){
                            postc.remove(n);
                            break;
                        }
                    }
                }
                // create the subgoal
				GoalNode subgoal = createGoal(depth+1, safeC, steps.get(i).getPreC(), steps.get(i).getPostC());
                // add the subgoal to the plan body
				planbody.add(subgoal);
			}
		}

		// add these to its plan body
		planNode.getPlanBody().addAll(planbody);
		// add the context condition
		planNode.getPre().addAll(prec);

		// remove the postcondition
		for(int m = 0; m < prec.size(); m++){
			for(int n = 0; n < postc.size(); n++){
				if(postc.get(n).getId().equals(prec.get(m).getId()) &&
				postc.get(n).getState() == prec.get(m).getState()){
					postc.remove(n);
					break;
				}
			}
		}
		return planNode;

	}


	/**
	 * a function to create a list execution steps in a plan body (we assume all these steps are actions)
	 * @param stepNum the number of steps
	 * @param prec the context condition of the plan
	 * @param gcs the goal condition this plan is going to achieve
	 * @param as the set of conditions that can be the postcondition of the actions
	 * @param steps the initially empty plan body
	 * @return
	 */
	private ArrayList<Literal> createPlanBody(int stepNum, ArrayList<Literal> prec, ArrayList<Literal> gcs, ArrayList<Literal> as, ArrayList<ActionNode> steps){
		// current states, copied from the precondition of the plan
		ArrayList<Literal> current = new ArrayList<>();
		for(int i = 0; i < prec.size(); i++){
			current.add(prec.get(i));
		}
		// possible action literals copied from as, we also ensure that there is no action make the current state true
		ArrayList<Literal> actionLiteral = new ArrayList<>(as);
		// construct each step
		for(int i = 0; i < stepNum; i++){
			// the precondition of the action
			ArrayList<Literal> precondition = new ArrayList<>();
			// the precondition of the first step is the same as the precondition of this plan
			if(i == 0){
				precondition = prec;
			}
			// if it is not then randomly select a literal from the current state.
			else {
				// select a literal
				int sx = rm.nextInt(current.size());
				// ignore the precondition that already appears
				while(precondition.contains(current.get(sx))){
					sx = rm.nextInt(current.size());
				}
				// add it to the set of preconditions
				precondition.add(current.get(sx));
			}

			// the postcondition of the action
			ArrayList<Literal> postcondition = new ArrayList<>();
			// if this step is the last action, then it has the goal-condition as its postcondition
			if(i == stepNum - 1){
				postcondition = gcs;
			}
			// otherwise, the postcondition is randomly generated apart from the last action
			else{
				// randomly select a postcondition
				int index = rm.nextInt(actionLiteral.size());
				Literal p = actionLiteral.get(index);
				postcondition.add(p);
				// update the current state
				updateCurrentLiterals(current, p);
				// update the set of action
				updateActionLiterals(actionLiteral, p);
			}
			// create the corresponding action
			ActionNode action = new ActionNode("", precondition, postcondition);
			steps.add(action);
		}

		// add the goal-condition
		for(int i = 0; i < gcs.size(); i++){
			current.add(gcs.get(i));
		}
		return current;
	}

	/**
	 * checks whether a literal l is in ls
	 * @param ls
	 * @param l
	 * @return
	 */
	private boolean contains(ArrayList<Literal> ls, Literal l){
		for(int i = 0; i < ls.size(); i++){
			if(ls.get(i).getId().equals(l.getId()) && ls.get(i).getState() == l.getState())
				return true;
		}
		return false;
	}

	/**
	 * update the current state based on a literal l. If a literal l(its negation) is in ls, then remove it.
	 * Add l in the tail of this list
	 * @param ls
	 * @param l
	 */
	private void updateCurrentLiterals(ArrayList<Literal> ls, Literal l){
		for(int i = 0; i < ls.size(); i++){
			if(ls.get(i).getId().equals(l.getId())){
				ls.remove(i);
				break;
			}
		}
		ls.add(l);
	}

	/**
	 * update the list of action literals. If a literal l is achieved, then remove l from ls and add its negation to ls
	 * @param ls
	 * @param l
	 */
	private void updateActionLiterals(ArrayList<Literal> ls, Literal l){
		int index = -1;
		for(int i = 0; i < ls.size(); i++){
			if(ls.get(i).getId().equals(l.getId())){
				// if they are exactly the same
				if(ls.get(i).getState() == l.getState()){
					// if its negation has not been found yet
					if(index == -1){
						index = i;
					}
					ls.remove(i);
					break;
				}
				// if its negation was found
				else{
					index = - 2;
				}
			}
		}

		if(index != -2){
			for(int i = index; i < ls.size(); i++){
				if(ls.get(i).getState() == l.getState()){
					index = -2;
					break;
				}
			}
		}

		if(index != -2) {
			ls.add(new Literal(l.getId(), !l.getState()));
		}
	}


	/**
	 * assign the types for each execution steps
	 * @param stepNum
	 * @return a list of boolean indicating the type of each step
	 */
	private ArrayList<Boolean> assignPosition(int stepNum){
		// a list of booleans
		ArrayList<Boolean> positions = new ArrayList<>();
		for(int i = 0; i < stepNum; i++){
			positions.add(true);
		}
		if(stepNum != this.num_action){
			ArrayList<Integer> goal_pos = new ArrayList<>();
			while (goal_pos.size() < this.num_goal){
				int index = rm.nextInt(stepNum-2);
				if(!goal_pos.contains(index+1)){
					goal_pos.add(index+1);
					positions.set(index+1,false);
				}
			}
		}
		return positions;
	}

	/**
	 * @param steps
	 * @param conds
	 * @return a list of safe literals which won't cause any conflicts
	 */
	private ArrayList<Literal> safeCondition(ArrayList<ActionNode> steps, ArrayList<Literal> conds){
		// clone the current action literals
		ArrayList<Literal> actionLiteral = (ArrayList<Literal>) conds.clone();

		// remove all conflicting conditions
		removeConflicting(actionLiteral, steps.get(0).getPreC());
		for(int i = 0; i < steps.size(); i++){
			removeConflicting(actionLiteral, steps.get(i).getPostC());
		}
		return actionLiteral;
	}

	/**
	 * remove all the conflicting literals
	 * @param ls
	 * @param l
	 */
	private void removeConflicting(ArrayList<Literal> ls, ArrayList<Literal> l){
		int index = ls.size();
		for(int j = 0; j < l.size(); j++){
			for(int i = 0; i < ls.size(); i++){
				if(ls.get(i).getId().equals(l.get(j).getId())) {
					ls.remove(i);
					index = i;
				}
			}
			for(int i = index; i < ls.size(); i++){
				if(ls.get(i).getId().equals(l.get(j).getId())) {
					ls.remove(i);
					break;
				}
			}
		}

	}


	/** Get the environmental or non-goal variables as ids */
	private List<String> getEnvLitsAsStrings(){
		// Make a new List from the literals of the environment
		List<String> envLits = new ArrayList<>(environment.keySet());
		// Filter out the goal Literals
		envLits.removeIf(l -> (l.startsWith("G-")));
		// Return this sublist
		return envLits;
	}
}
