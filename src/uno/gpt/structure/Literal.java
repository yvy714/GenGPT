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
/**
 * @version 3.0
 */
public class Literal implements Comparable<Literal> {

	/**
	 * identity of the literal, i.e., its name
	 */
	final private String id;
	/**
	 * state of the literal, i.e., its value
	 */
	private boolean state;

	/**
	 * constructor
	 * @param id
	 * @param state
	 */
	public Literal(String id, boolean state) {
		this.id = id;
		this.state = state;
	}

	/**
	 * @return the id of this literal
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the state of this literal
	 */
	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	/**
	 * flip the state of this literal
	 * @return the state after flipping
	 */
	public boolean flip(){
		this.state = !this.state;
		return this.state;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Literal literal = (Literal) o;

		return id.equals(literal.id) && (state == literal.state);
	}

	@Override
	public int compareTo(Literal o) {
		return this.getId().compareTo(o.getId());
	}

	/** write the literal*/
	public String toSimpleString()
	{
		return "(" + this.id + "," + this.state + ")";
	}

	@Override
	public Literal clone(){
		return new Literal(id, state);
	}
}
