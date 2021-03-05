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
import java.util.ArrayList;
import java.util.HashMap;

import uno.gpt.structure.*;

/**
 * @version 3.0
 *
 * flex the limitation of strictly sequential execution
 */
class Main
{
	/**
	 * The main function, takes a set of commandline arguments
	 */
	public static void main(String[] args)
	{
		// Shared parameters with their default values
		int seed = AbstractGenerator.def_seed, num_tree = AbstractGenerator.def_num_tree;
		//Synth parameters with their default values
		int sy_depth = SynthGenerator.def_depth,
				sy_num_goal = SynthGenerator.def_num_goal,
				sy_num_plan = SynthGenerator.def_num_plan,
				sy_num_action = SynthGenerator.def_num_action,
				sy_num_var = SynthGenerator.def_num_var,
				sy_num_selected = SynthGenerator.def_num_selected;
		double sy_prob_leaf = SynthGenerator.def_prob_leaf;
		// default output path
		String path ="gpt.xml";


		// help info
		String help = "\n" +
				"HELP:\n" +
				"-s\n Random seed. If the value is not specified, 100 is default \n" +
				"-d\n Maximum depth of the goal-plan tree. If the value is not specified, 3 is default.\n" +
				"-g\n Number of subgoals in each plan (except the leaf plan). If the value is not specified, 3 is default.\n" +
				"-p\n Number of plans to achieve each goal. If the value is not specified, 3 is default.\n" +
				"-a\n Number of actions in each plan. If the value is not specified, 3 is default.\n" +
				"-l\n Probability of a plan being leaf plan. If the value is not specified, 0 is default.\n" +
				"-v\n Number of environment variables. If the value is not specified, 60 is default.\n" +
				"-e\n Number of selected literals. If the value is not specified, 30 is default.\n" +
				"-t\n Number of goal-plan trees. If the value is not specified, 10 is default.\n" +
				"-f\n The output file path to which the set of goal-plan tree is saved. If the value is not specified, gpt.xml is default.\n";

		// parser for each input parameters
		int i = 0;
		String arg;
		// parse parameters one by one
		while(i < args.length && args[i].startsWith("-")) {
			// get the ith parameter
			arg = args[i++];
			// flag is in the form "-x"
			if(arg.length() != 2) {
				System.out.println(arg + " is not a valid flag");
				System.out.println(help);
				System.exit(1);
			}
			// get the flag
			char flag = arg.charAt(1);
			switch (flag){
				// h for help
				case 'h': // help
					System.out.println(help);
					System.exit(0);

				case 's': // seed
					try {
						seed = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("Seed must be an integer");
						System.exit(1);
					}

				case 'd': // depth
					try {
						sy_depth = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("Depth must be an integer");
						System.exit(1);
					}

				case 'g': // number of goals
					try {
						sy_num_goal = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("The number of goals in each plan must be an integer");
						System.exit(1);
					}
				case 'p': // number of plans
					try {
						sy_num_plan = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("The number of plans to achieve each goal must be an integer");
						System.exit(1);
					}
				case 'a': // number of actions
					try {
						sy_num_action = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("The number of actions in each plan must be an integer");
						System.exit(1);
					}
				case 'v': // number of variables
					try {
						sy_num_var = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("The number of environment variables must be an integer");
						System.exit(1);
					}
				case 'e': // selected number of variables
					try {
						sy_num_selected = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("The number of selected environment variables must be an integer");
						System.exit(1);
					}
				case 'l': // probability of a plan being leaf plan
					try {
						sy_prob_leaf = Double.parseDouble(args[i++]);break;
					}
					catch(Exception e) {
						System.out.println("The probability of a plan being leaf plan must be a double");
						System.exit(1);
					}
				case 't': // number of trees
					try {
						num_tree = Integer.parseInt(args[i++]);break;
					}
					catch(Exception e){
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

			// check the value of the input arguments
			if(sy_depth <= 0) {
				System.out.println("Depth must be greater than 0");
				System.exit(1);
			}
			if(sy_num_goal <= 0) {
				System.out.println("Maximum number of goals must be greater than 0");
				System.exit(1);
			}
			if(sy_num_plan <= 0) {
				System.out.println("Maximum number of plans must be greater than 0");
				System.exit(1);
			}
			if(sy_num_action < 0) {
				System.out.println("Maximum number of actions must be greater than 0");
				System.exit(1);
			}
			if(sy_num_var <= 0) {
				System.out.println("Total number of variables must be greater than 0");
				System.exit(1);
			}
			if(sy_num_selected <= 0) {
						System.out.println("The number of selected variables must be greater than 0");
						System.exit(1);
			}
			if(sy_num_selected > sy_num_var)
			{
				System.out.println("The number of selected variables must be less than or equal to the total number of variables");
				System.exit(1);
			}
			if(sy_prob_leaf < 0 || sy_prob_leaf > 1){
				System.out.println("probability must be between 0 and 1");
				System.exit(1);
			}
			if(num_tree <= 0) {
				System.out.println("Total number of goal-plan tree must be greater than 0");
				System.exit(1);
			}

			System.out.println("seed: " + seed);
			System.out.println("depth: " + sy_depth);
			System.out.println("tree: " + num_tree);
			System.out.println("goals: " + sy_num_goal);
			System.out.println("plans: " + sy_num_plan);
			System.out.println("actions: " + sy_num_action);
			System.out.println("var: " + sy_num_var);
			System.out.println("selected: " + sy_num_selected);
			System.out.println("prob_leaf: " + sy_prob_leaf);

		}
		/**
		 * the generator
		 */
		GPTGenerator gen = new SynthGenerator(seed, sy_depth, num_tree, sy_num_goal, sy_num_plan, sy_num_action, sy_num_var, sy_num_selected, sy_prob_leaf);;


		HashMap<String, Literal> environment = gen.genEnvironment();
		// generate the tree
		ArrayList<GoalNode> goalForests = new ArrayList<>();

		System.out.println(num_tree);
		for(int k = 0; k < num_tree; k++)
		{
			goalForests.add(gen.genTopLevelGoal(k));
		}
		// write the set of goal plan tree to an XML file
		XMLWriter wxf = new XMLWriter();
		wxf.CreateXML(environment, goalForests, path);
	}
	
}
