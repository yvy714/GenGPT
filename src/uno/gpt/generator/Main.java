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

import uno.gpt.nodes.*;

/**
 * @version 1.0
 */
public class Main 
{

	/**
	 * @param depth The depth of the tree.
	 * @param num_goal The number of goals in each plan
	 * @param num_plan The number of plans to acheive each goal
	 * @param num_action The number of actions in each plan
	 * @param num_var The number of variables
	 * @param num_tree The number of trees
	 * @param prob_parallel The probability of actions and goals executed in parallel
	 * @param path The path of the output file
	 */
	
	public static void main(String[] args) 
	{	
		// parameters with their default values
		int depth = 3, num_goal = 1,  num_plan = 2, num_action = 1, num_var = 50, num_tree = 1;
		double prob_parallel = 0;
		String path ="gpt.xml";
		
		String help = "\n" +
				  "HELP:\n" +
				  "-d\n Depth of the goal-plan tree. If the value is not specified, 3 is default.\n" +
			      "-g\n Number of subgoals in each plan (except the leaf plan). If the value is not specified, 1 is default.\n" +
			      "-p\n Number of plans to achieve each goal. If the value is not specified, 2 is default.\n" + 
			      "-a\n Number of actions in each plan. If the value is not specified, 1 is default.\n" +
			      "-l\n Probability that acitons and subgoals in a plan form part of a parallel composition. If the value is not specified, 0 is default.\n" +
			      "-v\n Number of environment variables. If the value is not specified, 50 is default.\n" + 
			      "-t\n Number of goal-plan trees. If the value is not specified, 1 is default.\n" + 
			      "-f\n The output file path to which the set of goal-plan tree is saved. If the value is not specified, gpt.xml is default.\n";
		
		// parser for the input parameters 
		int i = 0;
		String arg;
		while(i < args.length && args[i].startsWith("-"))
		{
			arg = args[i++];
			if(arg.length() != 2)
			{
				System.out.println(arg + " is not a valid flag");
				System.out.println(help);
				
				System.exit(1);
			}
			char flag = arg.charAt(1);
			switch (flag)
			{
				case 'h': // help
					System.out.println(help);
					System.exit(1);
					
			
				case 'd': // depth
						try
						{
							depth = Integer.parseInt(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("Depth must be an integer");
							System.exit(1);
						}
				case 'g': // number of goals
						try
						{
							num_goal = Integer.parseInt(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("The number of goals in each plan must be an integer");
							System.exit(1);
						}
				case 'p': // number of plans
						try
						{
							num_plan = Integer.parseInt(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("The number of plans to achieve each goal must be an integer");
							System.exit(1);
						}
				case 'a': // number of actions
						try
						{
							num_action = Integer.parseInt(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("The number of actions in each plan must be an integer");
							System.exit(1);
						}
				case 'l': // probability of parallel execution
						try
						{
							prob_parallel = Double.parseDouble(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("The probability must be a double");
							System.exit(1);
						}
				case 'v': // number of variables
						try
						{
							num_var = Integer.parseInt(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("The number of environment variables must be an integer");
							System.exit(1);
						}	
				case 't': // number of trees
						try
						{
							num_tree = Integer.parseInt(args[i++]);break;
						}catch(Exception e)
						{
							System.out.println("The number of goal-plan tree must be an integer");
							System.exit(1);
						}	
				case 'f': // path
						path = args[i++];break;
				default:
						System.out.println(arg + " is not a valid flag");
						System.out.println(help);
						System.exit(1);
			}
		}
		
		// check the value of the input arguments
		if(depth <= 0) 
		{ 
			System.out.println("Depth must be greater than 0");
			System.exit(1);
		}
		if(num_goal <= 0)
		{
			System.out.println("Maximum number of goals must be greater than 0");
			System.exit(1);
		}
		if(num_plan <= 0)
		{
			System.out.println("Maximum number of plans must be greater than 0");
			System.exit(1);
		}
		if(num_action < 0)
		{
			System.out.println("Maximum number of actions must be greater than or equal to 0");
			System.exit(1);
		}
		if(num_var <= 0)
		{
			System.out.println("Total number of variables must be greater than 0");
			System.exit(1);
		}
		if(num_tree <= 0)
		{
			System.out.println("Total number of goal-plan tree must be greater than 0");
			System.exit(1);
		}
		
		Generator ge = new Generator(depth, num_goal, num_plan, num_action, prob_parallel, num_var);
		// generate the tree
		ArrayList<GoalNode> goalForests = new ArrayList<GoalNode>();
		for(int k = 0; k < num_tree; k++)
		{
			goalForests.add(ge.genTopLevelGoal(k));
		}
		// write the set of goal plan tree to an XML file
		XMLWriter wxf = new XMLWriter();
		wxf.CreateXML(goalForests, path);	
	}
	
}
