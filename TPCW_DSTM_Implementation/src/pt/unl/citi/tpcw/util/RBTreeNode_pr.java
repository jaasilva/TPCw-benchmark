package pt.unl.citi.tpcw.util;

import org.deuce.distribution.replication.partial.Partial;

public class RBTreeNode_pr
{
	int k; // key
	@Partial
	Object v; // val
	RBTreeNode_pr p; // parent
	RBTreeNode_pr l; // left
	RBTreeNode_pr r; // right
	int c; // color

	public RBTreeNode_pr()
	{
	}
}
