package pt.unl.citi.tpcw.util;

import java.util.List;
import java.util.Stack;

import pt.unl.citi.tpcw.entities.Filter;

/*
 * =============================================================================
 * rbtree.java -- Red-black balanced binary search tree
 * =============================================================================
 * Copyright (C) Sun Microsystems Inc., 2006. All Rights Reserved. Authors: Dave
 * Dice, Nir Shavit, Ori Shalev. STM: Transactional Locking for Disjoint Access
 * Parallelism Transactional Locking II, Dave Dice, Ori Shalev, Nir Shavit DISC
 * 2006, Sept 2006, Stockholm, Sweden.
 * =============================================================================
 * Modified by Chi Cao Minh
 * =============================================================================
 * For the license of bayes/sort.h and bayes/sort.c, please see the header of
 * the files.
 * ------------------------------------------------------------------------ For
 * the license of kmeans, please see kmeans/LICENSE.kmeans
 * ------------------------------------------------------------------------ For
 * the license of ssca2, please see ssca2/COPYRIGHT
 * ------------------------------------------------------------------------ For
 * the license of lib/mt19937ar.c and lib/mt19937ar.h, please see the header of
 * the files.
 * ------------------------------------------------------------------------ For
 * the license of lib/rbtree.h and lib/rbtree.c, please see
 * lib/LEGALNOTICE.rbtree and lib/LICENSE.rbtree
 * ------------------------------------------------------------------------
 * Unless otherwise noted, the following license applies to STAMP files:
 * Copyright (c) 2007, Stanford University All rights reserved. Redistribution
 * and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: * Redistributions
 * of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. * Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of Stanford University nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission. THIS SOFTWARE
 * IS PROVIDED BY STANFORD UNIVERSITY ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL STANFORD UNIVERSITY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */

public class RBTree
{
	public static final int BLACK = 0;
	public static final int RED = 1;

	RBTreeNode root;
	int compID;

	public RBTree()
	{
	}

	/* private Methods */
	/* lookup */
	private RBTreeNode lookup(int k)
	{
		RBTreeNode p = root;

		while (p != null)
		{
			int cmp = compare(k, p.k);
			if (cmp == 0)
			{
				return p;
			}
			p = (cmp < 0) ? p.l : p.r;
		}

		return null;
	}

	/* rotateLeft */
	private void rotateLeft(RBTreeNode x)
	{
		RBTreeNode r = x.r;
		RBTreeNode rl = r.l;
		x.r = rl;
		if (rl != null)
		{
			rl.p = x;
		}

		RBTreeNode xp = x.p;
		r.p = xp;
		if (xp == null)
		{
			root = r;
		}
		else if (xp.l == x)
		{
			xp.l = r;
		}
		else
		{
			xp.r = r;
		}
		r.l = x;
		x.p = r;
	}

	/* rotateRight */
	private void rotateRight(RBTreeNode x)
	{
		RBTreeNode l = x.l;
		RBTreeNode lr = l.r;
		x.l = lr;
		if (lr != null)
		{
			lr.p = x;
		}
		RBTreeNode xp = x.p;
		l.p = xp;
		if (xp == null)
		{
			root = l;
		}
		else if (xp.r == x)
		{
			xp.r = l;
		}
		else
		{
			xp.l = l;
		}

		l.r = x;
		x.p = l;
	}

	/* parentOf */
	private RBTreeNode parentOf(RBTreeNode n)
	{
		return ((n != null) ? n.p : null);
	}

	/* leftOf */
	private RBTreeNode leftOf(RBTreeNode n)
	{
		return ((n != null) ? n.l : null);
	}

	/* rightOf */
	private RBTreeNode rightOf(RBTreeNode n)
	{
		return ((n != null) ? n.r : null);
	}

	/* colorOf */
	private int colorOf(RBTreeNode n)
	{
		return ((n != null) ? n.c : BLACK);
	}

	/* setColor */
	private void setColor(RBTreeNode n, int c)
	{
		if (n != null)
		{
			n.c = c;
		}
	}

	/* fixAfterInsertion */
	private void fixAfterInsertion(RBTreeNode x)
	{
		x.c = RED;

		while (x != null && x != root)
		{
			RBTreeNode xp = x.p;
			if (xp.c != RED)
			{
				break;
			}

			if (parentOf(x) == leftOf(parentOf(parentOf(x))))
			{
				RBTreeNode y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED)
				{
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				}
				else
				{
					if (x == rightOf(parentOf(x)))
					{
						x = parentOf(x);
						rotateLeft(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null)
					{
						rotateRight(parentOf(parentOf(x)));
					}
				}
			}
			else
			{
				RBTreeNode y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED)
				{
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				}
				else
				{
					if (x == leftOf(parentOf(x)))
					{
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null)
					{
						rotateLeft(parentOf(parentOf(x)));
					}
				}
			}
		}

		RBTreeNode ro = root;
		if (ro.c != BLACK)
		{
			ro.c = BLACK;
		}
	}

	private RBTreeNode insert(int k, Object v, RBTreeNode n)
	{
		RBTreeNode t = root;
		if (t == null)
		{
			if (n == null)
			{
				return null;
			}
			/* Note: the following STs don't really need to be transactional */
			n.l = null;
			n.r = null;
			n.p = null;
			n.k = k;
			n.v = v;
			n.c = BLACK;
			root = n;
			return null;
		}

		while (true)
		{
			int cmp = compare(k, t.k);
			if (cmp == 0)
			{
				return t;
			}
			else if (cmp < 0)
			{
				RBTreeNode tl = t.l;
				if (tl != null)
				{
					t = tl;
				}
				else
				{
					n.l = null;
					n.r = null;
					n.k = k;
					n.v = v;
					n.p = t;
					t.l = n;
					fixAfterInsertion(n);
					return null;
				}
			}
			else
			{ /* cmp > 0 */
				RBTreeNode tr = t.r;
				if (tr != null)
				{
					t = tr;
				}
				else
				{
					n.l = null;
					n.r = null;
					n.k = k;
					n.v = v;
					n.p = t;
					t.r = n;
					fixAfterInsertion(n);
					return null;
				}
			}
		}
	}

	/* successor */
	private RBTreeNode successor(RBTreeNode t)
	{
		if (t == null)
		{
			return null;
		}
		else if (t.r != null)
		{
			RBTreeNode p = t.r;
			while (p.l != null)
			{
				p = p.l;
			}
			return p;
		}
		else
		{
			RBTreeNode p = t.p;
			RBTreeNode ch = t;
			while (p != null && ch == p.r)
			{
				ch = p;
				p = p.p;
			}
			return p;
		}

	}

	/* fixAfterDeletion */
	private void fixAfterDeletion(RBTreeNode x)
	{
		while (x != root && colorOf(x) == BLACK)
		{
			if (x == leftOf(parentOf(x)))
			{
				RBTreeNode sib = rightOf(parentOf(x));
				if (colorOf(sib) == RED)
				{
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}
				if (colorOf(leftOf(sib)) == BLACK
						&& colorOf(rightOf(sib)) == BLACK)
				{
					setColor(sib, RED);
					x = parentOf(x);
				}
				else
				{
					if (colorOf(rightOf(sib)) == BLACK)
					{
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			}
			else
			{ /* symmetric */
				RBTreeNode sib = leftOf(parentOf(x));
				if (colorOf(sib) == RED)
				{
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}
				if (colorOf(rightOf(sib)) == BLACK
						&& colorOf(leftOf(sib)) == BLACK)
				{
					setColor(sib, RED);
					x = parentOf(x);
				}
				else
				{
					if (colorOf(leftOf(sib)) == BLACK)
					{
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}

		if (x != null && x.c != BLACK)
		{
			x.c = BLACK;
		}
	}

	private RBTreeNode deleteNode(RBTreeNode p)
	{
		/*
		 * If strictly internal, copy successor's element to p and then make p
		 * point to successor
		 */
		if (p.l != null && p.r != null)
		{
			RBTreeNode s = successor(p);
			p.k = s.k;
			p.v = s.v;
			p = s;
		} /* p has 2 children */

		/* Start fixup at replacement node, if it exists */
		RBTreeNode replacement = (p.l != null) ? p.l : p.r;

		if (replacement != null)
		{
			/* Link replacement to parent */
			replacement.p = p.p;
			RBTreeNode pp = p.p;
			if (pp == null)
			{
				root = replacement;
			}
			else if (p == pp.l)
			{
				pp.l = replacement;
			}
			else
			{
				pp.r = replacement;
			}

			/* Null out links so they are OK to use by fixAfterDeletion */
			p.l = null;
			p.r = null;
			p.p = null;

			/* Fix replacement */
			if (p.c == BLACK)
			{
				fixAfterDeletion(replacement);
			}
		}
		else if (p.p == null)
		{ /* return if we are the only node */
			root = null;
		}
		else
		{ /* No children. Use self as phantom replacement and unlink */
			if (p.c == BLACK)
			{
				fixAfterDeletion(p);
			}
			RBTreeNode pp = p.p;
			if (pp != null)
			{
				if (p == pp.l)
				{
					pp.l = null;
				}
				else if (p == pp.r)
				{
					pp.r = null;
				}
				p.p = null;
			}
		}
		return p;
	}

	/*
	 * Diagnostic section
	 */

	/* firstEntry */

	private RBTreeNode firstEntry()
	{
		RBTreeNode p = root;
		if (p != null)
		{
			while (p.l != null)
			{
				p = p.l;
			}
		}
		return p;
	}

	/* verifyRedBlack */

	private int verifyRedBlack(RBTreeNode root, int depth)
	{
		int height_left;
		int height_right;

		if (root == null)
		{
			return 1;
		}

		height_left = verifyRedBlack(root.l, depth + 1);
		height_right = verifyRedBlack(root.r, depth + 1);
		if (height_left == 0 || height_right == 0)
		{
			return 0;
		}
		if (height_left != height_right)
		{
			System.out.println(" Imbalace @depth = " + depth + " : "
					+ height_left + " " + height_right);
		}

		if (root.l != null && root.l.p != root)
		{
			System.out.println(" lineage");
		}
		if (root.r != null && root.r.p != root)
		{
			System.out.println(" lineage");
		}

		/* Red-Black alternation */
		if (root.c == RED)
		{
			if (root.l != null && root.l.c != BLACK)
			{
				System.out.println("VERIFY in verifyRedBlack");
				return 0;
			}

			if (root.r != null && root.r.c != BLACK)
			{
				System.out.println("VERIFY in verifyRedBlack");
				return 0;
			}
			return height_left;
		}
		if (root.c != BLACK)
		{
			System.out.println("VERIFY in verifyRedBlack");
			return 0;
		}

		return (height_left + 1);
	}

	/* compareKeysDefault */
	private int compare(int a, int b)
	{
		return a - b;
	}

	/*****************************************
	 * public methods
	 *****************************************/

	/*
	 * ==========================================================================
	 * === rbtree_verify
	 * ========================================================
	 * ===================== long rbtree_verify (rbtree_t* s, long verbose);
	 */
	public int verify(int verbose)
	{
		if (root == null)
		{
			return 1;
		}
		if (verbose != 0)
		{
			System.out.println("Integrity check: ");
		}

		if (root.p != null)
		{
			System.out.println("  (WARNING) root = " + root + " parent = "
					+ root.p);
			return -1;
		}
		if (root.c != BLACK)
		{
			System.out.println("  (WARNING) root = " + root + " color = "
					+ root.c);
		}

		/* Weak check of binary-tree property */
		int ctr = 0;
		RBTreeNode its = firstEntry();
		while (its != null)
		{
			ctr++;
			RBTreeNode child = its.l;
			if (child != null && child.p != its)
			{
				System.out.println("bad parent");
			}
			child = its.r;
			if (child != null && child.p != its)
			{
				System.out.println("Bad parent");
			}
			RBTreeNode nxt = successor(its);
			if (nxt == null)
			{
				break;
			}
			if (compare(its.k, nxt.k) >= 0)
			{
				System.out.println("Key order " + its + " (" + its.k + " "
						+ its.v + ") " + nxt + " (" + nxt.k + " " + nxt.v
						+ ") ");
				return -3;
			}
			its = nxt;
		}

		int vfy = verifyRedBlack(root, 0);
		if (verbose != 0)
		{
			System.out.println(" Nodes = " + ctr + " Depth = " + vfy);
		}

		return vfy;

	}

	/*
	 * ==========================================================================
	 * === rbtree_alloc
	 * ==========================================================
	 * =================== rbtree_t* rbtree_alloc (long (*compare)(const void*,
	 * const void*));
	 */
	public static RBTree alloc(int compID)
	{
		RBTree n = new RBTree();
		if (n != null)
		{
			n.compID = compID;
			n.root = null;
		}

		return n;
	}

	/*
	 * ==========================================================================
	 * === rbtree_free
	 * ==========================================================
	 * =================== void rbtree_free (rbtree_t* r);
	 */

	/*
	 * ==========================================================================
	 * === rbtree_insert -- Returns TRUE on success
	 * ==============================
	 * =============================================== bool_t rbtree_insert
	 * (rbtree_t* r, void* key, void* val);
	 */
	public boolean insert(int key, Object val)
	{
		RBTreeNode node = new RBTreeNode();
		RBTreeNode ex = insert(key, val, node);
		if (ex != null)
		{
			node = null;
		}
		return ex == null;
	}

	/*
	 * ==========================================================================
	 * === rbtree_delete
	 * ========================================================
	 * ===================== bool_t rbtree_delete (rbtree_t* r, void* key);
	 */
	public boolean remove(int key)
	{
		RBTreeNode node = null;
		node = lookup(key);

		if (node != null)
		{
			node = deleteNode(node);
		}
		// if(node != null) {
		// this should do a release
		// }
		return node != null;
	}

	/*
	 * ==========================================================================
	 * === rbtree_update -- Return FALSE if had to insert node first
	 * ============
	 * ================================================================= bool_t
	 * rbtree_update (rbtree_t* r, void* key, void* val);
	 */
	public boolean update(int key, Object val)
	{
		RBTreeNode nn = new RBTreeNode();
		RBTreeNode ex = insert(key, val, nn);
		if (ex != null)
		{
			ex.v = val;
			nn = null;
			return true;
		}
		return false;
	}

	/*
	 * ==========================================================================
	 * === rbtree_get
	 * ============================================================
	 * ================= void* rbtree_get (rbtree_t* r, void* key);
	 */
	public Object find(int key)
	{
		RBTreeNode n = lookup(key);
		if (n != null)
		{
			Object val = n.v;
			return val;
		}
		return null;
	}

	public <T> T find(Filter<T> f)
	{
		Stack<RBTreeNode> stack = new java.util.Stack<RBTreeNode>();
		if (root != null)
			stack.push(root);
		while (!stack.isEmpty())
		{
			RBTreeNode n = stack.pop();
			T v = (T) n.v;
			if (f.filter(v))
			{
				// return ((Copyable<T>) v).copy();
				return v;
			}
			else
			{
				RBTreeNode l = n.l;
				if (l != null)
					stack.push(l);
				RBTreeNode r = n.r;
				if (r != null)
					stack.push(r);
			}
		}
		return null;
	}

	public <T> List<T> findAll(Filter<T> f)
	{
		Stack<RBTreeNode> stack = new java.util.Stack<RBTreeNode>();
		List<T> results = new java.util.LinkedList<T>();
		if (root != null)
			stack.push(root);
		while (!stack.isEmpty())
		{
			RBTreeNode n = stack.pop();
			T v = (T) n.v;
			if (f.filter(v))
				// results.add(((Copyable<T>)v).copy());
				results.add(v);
			RBTreeNode l = n.l;
			if (l != null)
				stack.push(l);
			RBTreeNode r = n.r;
			if (r != null)
				stack.push(r);
		}
		return results;
	}

	/*
	 * ==========================================================================
	 * === rbtree_contains
	 * ======================================================
	 * ======================= bool_t rbtree_contains (rbtree_t* r, void* key);
	 */
	public boolean contains(int key)
	{
		RBTreeNode n = lookup(key);

		return (n != null);
	}
	
	public void clear()
	{
		this.root = null;
	}

}

/*
 * =============================================================================
 * End of rbtree.java
 * =============================================================================
 */
