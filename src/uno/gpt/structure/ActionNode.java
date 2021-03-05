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
 */
public class ActionNode extends Node{
	// Action -> ActionName {Pre-condition}{post-condition}
	/** precondition */
	final private ArrayList<Literal> prec;
	/** postcondition */
	final private ArrayList<Literal> postc;

	public ActionNode(String name){
		super(name);
		this.prec = new ArrayList<>();
		this.postc = new ArrayList<>();
	}

	public ActionNode(String name, ArrayList<Literal> precondition, ArrayList<Literal> postcondition) {
		super(name);
		this.prec = precondition;
		this.postc = postcondition;
	}
	
	/** method to return the precondition of this action */
	public ArrayList<Literal> getPreC()
	{
		return this.prec;
	}
	
	/** method to return the postcondition of this action */
	public ArrayList<Literal> getPostC()
	{
		return this.postc;
	}
	
}
