package soot.tagkit;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Archie L. Cobbs
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import soot.jimple.DoubleConstant;

public class DoubleConstantValueTag extends ConstantValueTag {

  public static final String NAME = "DoubleConstantValueTag";

  private final double value;

  public DoubleConstantValueTag(double val) {
    super(null);
    this.value = val;
  }

  public double getDoubleValue() {
    return value;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String toString() {
    return "ConstantValue: " + Double.toString(value);
  }

  @Override
  public DoubleConstant getConstant() {
    return DoubleConstant.v(value);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp = Double.doubleToLongBits(value);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj) || (this.getClass() != obj.getClass())) {
      return false;
    }
    DoubleConstantValueTag other = (DoubleConstantValueTag) obj;
    return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
  }
}
