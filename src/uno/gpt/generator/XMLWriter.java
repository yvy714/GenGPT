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

import java.io.*;
import java.util.ArrayList;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import uno.gpt.nodes.*;

/**
 * @version 1.0
 */
public class XMLWriter
{	
	public XMLWriter()
	{
	}
	
	/**
	 * Write to the XML file 
	 * @param input The set of goal-plan tree
	 * @param ph The path
	 */
	public void CreateXML(ArrayList<GoalNode> input, String ph)
	{
		try
		{
			Element forest = new Element("Forest");
			Document document = new Document(forest);
			// write each top-level goals
			for(int i = 0; i < input.size(); i++)
			{
				GoalNode gl = input.get(i);
				writeGoal(gl, forest);
			}
			
			XMLOutputter xmlOutputer = new XMLOutputter();
			xmlOutputer.setFormat(Format.getPrettyFormat());
			xmlOutputer.output(document, new FileWriter(ph));
			System.out.println("XML File was created successfully!");
		}
		catch(IOException ex)
		{
			
		}
	}
	
	/**
	 * write a plan
	 * @param pl The target plan
	 * @param parent The goal to achieve
	 */
	public void writePlan(PlanNode pl, Element parent)
	{
		Element plan = new Element("Plan");
		plan.setAttribute(new Attribute("name", pl.getName()));
		// precondition
		ArrayList<Literal> st = pl.getPre();
		
		if(st == null || st.size() == 0)
		{
			plan.setAttribute(new Attribute("precondition", "null"));
		}
		else
		{
			String pre = "";
			for(int i = 0 ; i < st.size(); i++)
			{
				pre+=st.get(i).toString();
			}
			plan.setAttribute(new Attribute("precondition", pre));
		}
		
		// in-condition
		st = pl.getInc();
				
		if(st == null || st.size() == 0)
		{
			plan.setAttribute(new Attribute("in-condition", "null"));
		}
		else
		{
			String inc = "";
			for(int i = 0 ; i < st.size(); i++)
			{
				inc+=st.get(i).toString();
			}
			plan.setAttribute(new Attribute("in-condition", inc));
		}
		
		// write all actions, subgoals and parallel compositions it contains
		for(int i = 0; i < pl.getPlanBody().size(); i++)
		{
			if(pl.getPlanBody().get(i) instanceof ActionNode)
			{
				ActionNode act = (ActionNode) pl.getPlanBody().get(i);
				writeAction(act, plan);
			}
			if(pl.getPlanBody().get(i) instanceof GoalNode)
			{
				GoalNode gl = (GoalNode) pl.getPlanBody().get(i);
				writeGoal(gl, plan);
			}
			if(pl.getPlanBody().get(i) instanceof ParallelNode)
			{
				ParallelNode pn = (ParallelNode) pl.getPlanBody().get(i);
				writeParallel(pn, plan);
			}
		}
		parent.addContent(plan);
	}
	
	/**
	 * write action
	 * @param act The target action
	 * @param parent The plan which contain this action
	 */
	public void writeAction(ActionNode act, Element parent)
	{
		Element action = new Element("Action");
		action.setAttribute(new Attribute("name", act.getName()));
		// pre-condition
		ArrayList<Literal> st = act.getPreC();
		if(st == null || st.size() == 0)
		{
			action.setAttribute(new Attribute("precondition", "null"));
		}
		else
		{
			String pre = "";
			for(int i = 0 ; i < st.size(); i++)
			{
				pre = pre + st.get(i).toString();
			}
			action.setAttribute(new Attribute("precondition", pre));
		}
		
		// in-condition
		st = act.getInC();
		if(st == null || st.size() == 0)
		{
			action.setAttribute(new Attribute("in-condition", "null"));
		}
		else
		{
			String inc = "";
			for(int i = 0 ; i < st.size(); i++)
			{
				inc = inc + st.get(i).toString();
			}
			action.setAttribute(new Attribute("in-condition", inc));
		} 			
		
		// postcondition
		st = act.getPostC();
		if(st == null)
		{
			action.setAttribute(new Attribute("postcondition", "null"));
		}
		else
		{
			String post = "";
			for(int i = 0 ; i < st.size(); i++)
			{
				post = post + st.get(i).toString();
			}
			action.setAttribute(new Attribute("postcondition", post));
		} 
		parent.addContent(action);
	}
	
	/**
	 * write goal
	 * @param gl The target goal
	 * @param parent the Plan which contain this goal
	 */
	public void writeGoal(GoalNode gl, Element parent)
	{
		Element goal = new Element("Goal");
		goal.setAttribute(new Attribute("name", gl.getName()));	
		
		// in-condition
		ArrayList<Literal> st = gl.getInC();
		if(st == null || st.size() == 0)
		{
			goal.setAttribute(new Attribute("in-condition", "null"));
		}
		else
		{
			String inc = "";
			for(int i = 0 ; i < st.size(); i++)
			{
				inc = inc + st.get(i).toString();
			}
			goal.setAttribute(new Attribute("in-condition", inc));
		} 
		
		for(int i = 0; i < gl.getPlans().size(); i++)
		{
			PlanNode pl = gl.getPlans().get(i);
			writePlan(pl, goal);
		}
		parent.addContent(goal);
	}
	
	/**
	 * write parallel composition
	 * @param pn The target parallel composition
	 * @param parent The plan which contain this parallel composition
	 */
	public void writeParallel(ParallelNode pn, Element parent)
	{
		Element parallel = new Element("Parallel_composition");
		parallel.setAttribute(new Attribute("name", pn.getName()));	
		
		for(int i = 0; i < pn.getParallel().size(); i++)
		{
			if(pn.getParallel().get(i) instanceof ActionNode)
			{
				ActionNode act = (ActionNode) pn.getParallel().get(i);
				writeAction(act, parallel);
			}
			if(pn.getParallel().get(i) instanceof GoalNode)
			{
				GoalNode gl = (GoalNode) pn.getParallel().get(i);
				writeGoal(gl, parallel);
			}
		}
		parent.addContent(parallel);
	}
	
}
