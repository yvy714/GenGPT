package uno.gpt.generators;

import uno.gpt.structure.GoalNode;
import uno.gpt.structure.Literal;

import java.util.HashMap;

interface GPTGenerator {

    HashMap<String, Literal> genEnvironment();

    GoalNode genTopLevelGoal(int index);

}
