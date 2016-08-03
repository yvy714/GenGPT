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

package uno.gpt.nodes;

import java.util.ArrayList;

/**
 * @version 1.0
 */
public class ActionNode extends Node{
	// Action -> ActionName {Pre-condition}{post-condition}
	/** precondition */
	private ArrayList<Literal> prec;
	
	/** in-condition */
	private ArrayList<Literal> inc;
	
	/** postcondition */
	private ArrayList<Literal> postc;


	public ActionNode(String name, ArrayList<Literal> precondition, ArrayList<Literal> incondition, 
			ArrayList<Literal> postcondition)
	{
		super(name);
		this.prec = precondition;
		this.inc = incondition;
		this.postc = postcondition;
	}
	
	/** method to return the precondition of this action */
	public ArrayList<Literal> getPreC()
	{
		return this.prec;
	}
	
	/** method to return the in-condition of this action */
	public ArrayList<Literal> getInC()
	{
		return this.inc;
	}
	
	/** method to return the postcondition of this action */
	public ArrayList<Literal> getPostC()
	{
		return this.postc;
	}
	
}
