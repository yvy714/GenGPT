/*
 * Copyright 2021 Yuan Yao
 * University of Nottingham
 * Zhejiang University of Technology
 * Email: yaoyuan@zjut.edu.cn (yuanyao1990yy@icloud.com)
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


package uno.gpt.structure;

import java.util.ArrayList;

/**
 * @version 3.0
 * @author yuanyao
 */
public class PlanNode extends Node
{
	// Plan -> PlanName {Context Condition} {PlanStep ; ... ; PlanStep}
	/** planbody */
	final private ArrayList<Node> pb;
	
	/** precondition */
	final private ArrayList<Literal> pre;


	public PlanNode(String name){
		super(name);
		this.pb = new ArrayList<>();
		this.pre = new ArrayList<>();
	}

	public PlanNode(String name, ArrayList<Node> planbody, ArrayList<Literal> precondition) {
		super(name);
		this.pb = planbody;
		this.pre = precondition;
	}

	/** method to return the precondition of this plan */
	public ArrayList<Literal> getPre()
	{
		return this.pre;
	}
	
	/** method to return its planbody */
	public ArrayList<Node> getPlanBody()
	{
		return this.pb;
	}

}
