package soot.jimple.toolkits.typing;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2000 Etienne Gagnon.  All rights reserved.
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

import java.util.Iterator;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.RefType;
import soot.options.Options;
import soot.util.BitSetIterator;
import soot.util.BitVector;

/**
 * Represents a type variable.
 * 
 * @deprecated use {@link soot.jimple.toolkits.typing.fast.TypeResolver} instead
 **/
@Deprecated
class TypeVariableBV implements Comparable<Object> {
  private static final Logger logger = LoggerFactory.getLogger(TypeVariableBV.class);
  private static final boolean DEBUG = false;

  private final int id;
  private final TypeResolverBV resolver;

  private TypeVariableBV rep = this;
  private int rank = 0;

  private TypeNode approx;

  private TypeNode type;
  private TypeVariableBV array;
  private TypeVariableBV element;
  private int depth;

  private BitVector parents = new BitVector();
  private BitVector children = new BitVector();
  private BitVector ancestors;
  private BitVector indirectAncestors;

  public TypeVariableBV(int id, TypeResolverBV resolver) {
    this.id = id;
    this.resolver = resolver;
  }

  public TypeVariableBV(int id, TypeResolverBV resolver, TypeNode type) {
    this.id = id;
    this.resolver = resolver;
    this.type = type;
    approx = type;

    for (Iterator<TypeNode> parentIt = type.parents().iterator(); parentIt.hasNext();) {

      final TypeNode parent = parentIt.next();

      addParent(resolver.typeVariable(parent));
    }

    if (type.hasElement()) {
      element = resolver.typeVariable(type.element());
      element.array = this;
    }
  }

  public int hashCode() {
    if (rep != this) {
      return ecr().hashCode();
    }

    return id;
  }

  public boolean equals(Object obj) {
    if (rep != this) {
      return ecr().equals(obj);
    }

    if ((obj == null) || !obj.getClass().equals(getClass())) {
      return false;
    }

    TypeVariableBV ecr = ((TypeVariableBV) obj).ecr();

    if (ecr != this) {
      return false;
    }

    return true;
  }

  public int compareTo(Object o) {
    if (rep != this) {
      return ecr().compareTo(o);
    }

    return id - ((TypeVariableBV) o).ecr().id;
  }

  private TypeVariableBV ecr() {
    if (rep != this) {
      rep = rep.ecr();
    }

    return rep;
  }

  public TypeVariableBV union(TypeVariableBV var) throws TypeException {
    if (rep != this) {
      return ecr().union(var);
    }

    TypeVariableBV y = var.ecr();

    if (this == y) {
      parents.clear(var.ownId());
      children.clear(var.ownId());
      return this;
    }

    if (rank > y.rank) {
      resolver.invalidateId(y.id());
      y.rep = this;

      merge(y);
      y.clear();

      return this;
    }

    resolver.invalidateId(this.id());
    rep = y;
    if (rank == y.rank) {
      y.rank++;
    }

    y.merge(this);
    clear();

    return y;
  }

  private void clear() {
    approx = null;
    type = null;
    element = null;
    array = null;
    parents = null;
    children = null;
    ancestors = null;
    indirectAncestors = null;
  }

  private void merge(TypeVariableBV var) throws TypeException {
    if (depth != 0 || var.depth != 0) {
      throw new InternalTypingException();
    }

    // Merge types
    if (type == null) {
      type = var.type;
    } else if (var.type != null) {
      error("Type Error(1): Attempt to merge two types.");
    }

    parents.or(var.parents);
    parents.clear(var.ownId());
    parents.clear(this.ownId());
    children.or(var.children);
    children.clear(var.ownId());
    children.clear(this.ownId());
  }

  void validate() throws TypeException {
    if (rep != this) {
      ecr().validate();
      return;
    }

    // Validate relations.
    if (type != null) {
      for (BitSetIterator i = parents.iterator(); i.hasNext();) {
        TypeVariableBV parent = resolver.typeVariableForId(i.next()).ecr();

        if (parent.type != null) {
          if (!type.hasAncestor(parent.type)) {
            if (DEBUG) {
              logger.debug("" + parent.type + " is not a parent of " + type);
            }

            error("Type Error(2): Parent type is not a valid ancestor.");
          }
        }
      }

      for (BitSetIterator i = children.iterator(); i.hasNext();) {
        TypeVariableBV child = resolver.typeVariableForId(i.next()).ecr();

        if (child.type != null) {
          if (!type.hasDescendant(child.type)) {
            if (DEBUG) {
              logger.debug("" + child.type + "(" + child + ") is not a child of " + type + "(" + this + ")");
            }

            error("Type Error(3): Child type is not a valid descendant.");
          }
        }
      }
    }
  }

  public void removeIndirectRelations() {
    if (rep != this) {
      ecr().removeIndirectRelations();
      return;
    }

    if (indirectAncestors == null) {
      fixAncestors();
    }

    BitVector parentsToRemove = new BitVector();
    for (BitSetIterator parentIt = parents.iterator(); parentIt.hasNext();) {

      final int parent = parentIt.next();
      if (indirectAncestors.get(parent)) {
        parentsToRemove.set(parent);
      }
    }

    for (BitSetIterator i = parentsToRemove.iterator(); i.hasNext();) {
      removeParent(resolver.typeVariableForId(i.next()));
    }
  }

  private void fixAncestors() {
    BitVector ancestors = new BitVector(0);
    BitVector indirectAncestors = new BitVector(0);
    fixParents();
    for (BitSetIterator i = parents.iterator(); i.hasNext();) {
      TypeVariableBV parent = resolver.typeVariableForId(i.next()).ecr();

      if (parent.ancestors == null) {
        parent.fixAncestors();
      }

      ancestors.set(parent.id);
      ancestors.or(parent.ancestors);
      indirectAncestors.or(parent.ancestors);
    }

    this.ancestors = ancestors;
    this.indirectAncestors = indirectAncestors;
  }

  private void fixParents() {
    if (rep != this) {
      ecr().fixParents();
    }

    BitVector invalid = new BitVector();
    invalid.or(parents);
    invalid.and(resolver.invalidIds());
    for (BitSetIterator i = invalid.iterator(); i.hasNext();) {
      parents.set(resolver.typeVariableForId(i.next()).id());
    }
    parents.clear(this.id);
    parents.clear(this.id());
    parents.andNot(invalid);
  }

  public int id() {
    if (rep != this) {
      return ecr().id();
    }

    return id;
  }

  public int ownId() {
    return id;
  }

  public void addParent(TypeVariableBV variable) {
    if (rep != this) {
      ecr().addParent(variable);
      return;
    }

    TypeVariableBV var = variable.ecr();

    if (var == this) {
      return;
    }

    parents.set(var.id);
    var.children.set(this.id);
  }

  public void removeParent(TypeVariableBV variable) {
    if (rep != this) {
      ecr().removeParent(variable);
      return;
    }

    parents.clear(variable.id());
    parents.clear(variable.ownId());
    variable.children().clear(this.id);
  }

  public void addChild(TypeVariableBV variable) {
    if (rep != this) {
      ecr().addChild(variable);
      return;
    }

    TypeVariableBV var = variable.ecr();

    if (var == this) {
      return;
    }

    children.set(var.id);
    parents.set(var.id);
  }

  public void removeChild(TypeVariableBV variable) {
    if (rep != this) {
      ecr().removeChild(variable);
      return;
    }

    TypeVariableBV var = variable.ecr();

    children.clear(var.id);
    var.parents.clear(var.id);
  }

  public int depth() {
    if (rep != this) {
      return ecr().depth();
    }

    return depth;
  }

  public void makeElement() {
    if (rep != this) {
      ecr().makeElement();
      return;
    }

    if (element == null) {
      element = resolver.typeVariable();
      element.array = this;
    }
  }

  public TypeVariableBV element() {
    if (rep != this) {
      return ecr().element();
    }

    return (element == null) ? null : element.ecr();
  }

  public TypeVariableBV array() {
    if (rep != this) {
      return ecr().array();
    }

    return (array == null) ? null : array.ecr();
  }

  public BitVector parents() {
    if (rep != this) {
      return ecr().parents();
    }

    return parents;
  }

  public BitVector children() {
    if (rep != this) {
      return ecr().children();
    }

    return children;
  }

  public TypeNode approx() {
    if (rep != this) {
      return ecr().approx();
    }

    return approx;
  }

  public TypeNode type() {
    if (rep != this) {
      return ecr().type();
    }

    return type;
  }

  static void error(String message) throws TypeException {
    try {
      throw new TypeException(message);
    } catch (TypeException e) {
      if (DEBUG) {
        logger.error(e.getMessage(), e);
      }
      throw e;
    }
  }

  /**
   * Computes approximative types. The work list must be initialized with all constant type variables.
   */
  public static void computeApprox(TreeSet<TypeVariableBV> workList) throws TypeException {
    while (workList.size() > 0) {
      TypeVariableBV var = workList.first();
      workList.remove(var);

      var.fixApprox(workList);
    }
  }

  private void fixApprox(TreeSet<TypeVariableBV> workList) throws TypeException {
    if (rep != this) {
      ecr().fixApprox(workList);
      return;
    }

    if (type == null && approx != resolver.hierarchy().NULL) {
      TypeVariableBV element = element();

      if (element != null) {
        if (!approx.hasElement()) {
          logger.debug("*** " + this + " ***");

          error("Type Error(4)");
        }

        TypeNode temp = approx.element();

        if (element.approx == null) {
          element.approx = temp;
          workList.add(element);
        } else {
          TypeNode type = element.approx.lca(temp);

          if (type != element.approx) {
            element.approx = type;
            workList.add(element);
          } else if (element.approx != resolver.hierarchy().INT) {
            type = approx.lca(element.approx.array());

            if (type != approx) {
              approx = type;
              workList.add(this);
            }
          }
        }
      }

      TypeVariableBV array = array();

      if (array != null && approx != resolver.hierarchy().NULL && approx != resolver.hierarchy().INT) {
        TypeNode temp = approx.array();

        if (array.approx == null) {
          array.approx = temp;
          workList.add(array);
        } else {
          TypeNode type = array.approx.lca(temp);

          if (type != array.approx) {
            array.approx = type;
            workList.add(array);
          } else {
            type = approx.lca(array.approx.element());

            if (type != approx) {
              approx = type;
              workList.add(this);
            }
          }
        }
      }
    }

    for (BitSetIterator i = parents.iterator(); i.hasNext();) {
      TypeVariableBV parent = resolver.typeVariableForId(i.next()).ecr();

      if (parent.approx == null) {
        parent.approx = approx;
        workList.add(parent);
      } else {
        TypeNode type = parent.approx.lca(approx);

        if (type != parent.approx) {
          parent.approx = type;
          workList.add(parent);
        }
      }
    }

    if (type != null) {
      approx = type;
    }
  }

  public void fixDepth() throws TypeException {
    if (rep != this) {
      ecr().fixDepth();
      return;
    }

    if (type != null) {
      if (type.type() instanceof ArrayType) {
        ArrayType at = (ArrayType) type.type();

        depth = at.numDimensions;
      } else {
        depth = 0;
      }
    } else {
      if (approx.type() instanceof ArrayType) {
        ArrayType at = (ArrayType) approx.type();

        depth = at.numDimensions;
      } else {
        depth = 0;
      }
    }

    // make sure array types have element type
    if (depth == 0 && element() != null) {
      error("Type Error(11)");
    } else if (depth > 0 && element() == null) {
      makeElement();
      TypeVariableBV element = element();
      element.depth = depth - 1;

      while (element.depth != 0) {
        element.makeElement();
        element.element().depth = element.depth - 1;
        element = element.element();
      }
    }
  }

  public void propagate() {
    if (rep != this) {
      ecr().propagate();
    }

    if (depth == 0) {
      return;
    }

    for (BitSetIterator i = parents.iterator(); i.hasNext();) {
      TypeVariableBV var = resolver.typeVariableForId(i.next()).ecr();

      if (var.depth() == depth) {
        element().addParent(var.element());
      } else if (var.depth() == 0) {
        if (var.type() == null) {
          // hack for J2ME library, reported by Stephen Cheng
          if (!Options.v().j2me()) {
            var.addChild(resolver.typeVariable(resolver.hierarchy().CLONEABLE));
            var.addChild(resolver.typeVariable(resolver.hierarchy().SERIALIZABLE));
          }
        }
      } else {
        if (var.type() == null) {
          // hack for J2ME library, reported by Stephen Cheng
          if (!Options.v().j2me()) {
            var.addChild(resolver.typeVariable(ArrayType.v(RefType.v("java.lang.Cloneable"), var.depth())));
            var.addChild(resolver.typeVariable(ArrayType.v(RefType.v("java.io.Serializable"), var.depth())));
          }
        }
      }
    }

    for (BitSetIterator varIt = parents.iterator(); varIt.hasNext();) {

      final TypeVariableBV var = resolver.typeVariableForId(varIt.next());
      removeParent(var);
    }
  }

  public String toString() {
    if (rep != this) {
      return ecr().toString();
    }

    StringBuffer s = new StringBuffer();
    s.append(",[parents:");

    {
      boolean comma = false;

      for (BitSetIterator i = parents.iterator(); i.hasNext();) {
        if (comma) {
          s.append(",");
        } else {
          comma = true;
        }
        s.append(i.next());
      }
    }

    s.append("],[children:");

    {
      boolean comma = false;

      for (BitSetIterator i = children.iterator(); i.hasNext();) {
        if (comma) {
          s.append(",");
        } else {
          comma = true;
        }
        s.append(i.next());
      }
    }

    s.append("]");
    return "[id:" + id + ",depth:" + depth + ((type != null) ? (",type:" + type) : "") + ",approx:" + approx + s
        + (element == null ? "" : ",arrayof:" + element.id()) + "]";
  }

}
