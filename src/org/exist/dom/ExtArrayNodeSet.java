/*
 * eXist Open Source Native XML Database Copyright (C) 2001-03 Wolfgang M.
 * Meier wolfgang@exist-db.org http://exist-db.org
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * $Id$
 */
package org.exist.dom;

import java.util.Iterator;
import org.exist.util.FastQSort;
import org.exist.util.Range;
import org.exist.util.hashtable.Int2ObjectHashMap;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.SequenceIterator;
import org.w3c.dom.Node;

/**
 * A fast node set implementation, based on arrays to store nodes and a hash
 * map to organize documents.
 * 
 * The class uses arrays to store all nodes belonging to one document. The hash
 * map maps the document id to the array containing the nodes for the document.
 * Nodes are just appended to the array. No order is guaranteed and calls to
 * get/contains may fail although a node is present in the array (get/contains
 * do a binary search and thus assume that the set is sorted). Also, duplicates
 * are allowed. If you have to ensure that calls to get/contains return valid
 * results at any time and no duplicates occur, use class
 * {@link org.exist.dom.AVLTreeNodeSet}.
 * 
 * Use this class, if you can either ensure that items are added in order, or
 * no calls to contains/get are required during the creation phase. Only after
 * a call to one of the iterator methods, the set will get sorted and
 * duplicates removed.
 * 
 * @author Wolfgang <wolfgang@exist-db.org>
 * @since 0.9.3
 */
public final class ExtArrayNodeSet extends AbstractNodeSet {

	private Int2ObjectHashMap map;
	private int initalSize = 128;
	private int size = 0;
	private boolean isSorted = false;
	private boolean isInDocumentOrder = false;
	private int lastDoc = -1;
	private Part lastPart = null;
	private int state = 0;

	public ExtArrayNodeSet() {
		this.map = new Int2ObjectHashMap(512);
	}

	/**
	 * Constructor for ExtArrayNodeSet.
	 * 
	 * The first argument specifies the expected number of documents in this
	 * node set. The second int argument specifies the default array size,
	 * which is used whenever a new array has to be allocated for nodes. The
	 * default array size can be overwritten by the sizeHint argument passed to
	 * {@link #add(NodeProxy, int).
	 * 
	 * @param initialDocsCount
	 * @param initialArraySize
	 */
	public ExtArrayNodeSet(int initialDocsCount, int initialArraySize) {
		this.initalSize = initialArraySize;
		this.map = new Int2ObjectHashMap(initialDocsCount);
	}

	public ExtArrayNodeSet(int initialArraySize) {
		this(512, initialArraySize);
	}

	public void add(NodeProxy proxy) {
		getPart(proxy.doc, true, initalSize).add(proxy);
		++size;
		isSorted = false;
		isInDocumentOrder = false;
		setHasChanged();
	}

	/**
	 * Add a new node to the set. If a new array of nodes has to be allocated
	 * for the document, use the sizeHint parameter to determine the size of
	 * the newly allocated array. This will overwrite the default array size.
	 * 
	 * If the size hint is correct, no further reallocations will be required.
	 */
	public void add(NodeProxy proxy, int sizeHint) {
		getPart(proxy.doc, true, sizeHint > -1 ? sizeHint : initalSize).add(
				proxy);
		++size;
		isSorted = false;
		isInDocumentOrder = false;
		setHasChanged();
	}

	private void setHasChanged() {
		state = (state == Integer.MAX_VALUE ? state = 0 : state + 1);
	}

	public int getSizeHint(DocumentImpl doc) {
		Part part = getPart(doc, false, 0);
		return part == null ? -1 : part.length;
	}

	private Part getPart(DocumentImpl doc, boolean create, int sizeHint) {
		if (doc.docId == lastDoc && lastPart != null)
			return lastPart;
		Part part = (Part) map.get(doc.docId);
		if (part == null && create) {
			part = new Part(sizeHint, doc);
			map.put(doc.docId, part);
		}
		lastPart = part;
		lastDoc = doc.docId;
		return part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#iterator()
	 */
	public Iterator iterator() {
		sort();
		return new ExtArrayIterator(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.xpath.value.Sequence#iterate()
	 */
	public SequenceIterator iterate() {
		sortInDocumentOrder();
		return new ExtArrayIterator(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.AbstractNodeSet#unorderedIterator()
	 */
	public SequenceIterator unorderedIterator() {
		sort();
		return new ExtArrayIterator(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#containsDoc(org.exist.dom.DocumentImpl)
	 */
	public boolean containsDoc(DocumentImpl doc) {
		return map.containsKey(doc.docId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#contains(org.exist.dom.DocumentImpl, long)
	 */
	public boolean contains(DocumentImpl doc, long nodeId) {
		final Part part = getPart(doc, false, 0);
		return part == null ? false : part.contains(nodeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#contains(org.exist.dom.NodeProxy)
	 */
	public boolean contains(NodeProxy proxy) {
		final Part part = getPart(proxy.doc, false, 0);
		return part == null ? false : part.contains(proxy.gid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#addAll(org.exist.dom.NodeSet)
	 */
	public void addAll(NodeSet other) {
		for (Iterator i = other.iterator(); i.hasNext();) {
			add((NodeProxy) i.next());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.xpath.value.Sequence#getLength()
	 */
	public int getLength() {
		sort(); // sort to remove duplicates
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.NodeList#item(int)
	 */
	public Node item(int pos) {
		sortInDocumentOrder();
		NodeProxy p = get(pos);
		return p == null ? null : p.getNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#get(int)
	 */
	public NodeProxy get(int pos) {
		int count = 0;
		Part part;
		for (Iterator i = map.valueIterator(); i.hasNext();) {
			part = (Part) i.next();
			if (count + part.length > pos)
				return part.get(pos - count);
			count += part.length;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#get(org.exist.dom.NodeProxy)
	 */
	public NodeProxy get(NodeProxy p) {
		final Part part = getPart(p.doc, false, 0);
		return part == null ? null : part.get(p.gid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#get(org.exist.dom.DocumentImpl, long)
	 */
	public NodeProxy get(DocumentImpl doc, long nodeId) {
		sort();
		final Part part = getPart(doc, false, 0);
		return part == null ? null : part.get(nodeId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.xpath.value.Sequence#itemAt(int)
	 */
	public Item itemAt(int pos) {
		sortInDocumentOrder();
		return get(pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.NodeSet#remove(org.exist.dom.NodeProxy)
	 */
	public void remove(NodeProxy node) {
		final Part part = getPart(node.doc, false, 0);
		if (part == null)
			return;
		part.remove(node);
		if (part.length == 0)
			map.remove(node.doc.getDocId());
		setHasChanged();
	}

	public NodeSet getRange(DocumentImpl doc, long lower, long upper) {
		final Part part = getPart(doc, false, 0);
		return part.getRange(lower, upper);
	}

	public NodeSet hasChildrenInSet(NodeProxy parent, int mode,
									boolean rememberContext) {
		final Part part = getPart(parent.doc, false, 0);
		if (part == null)
			return new ArraySet(1);
		return part.getChildrenInSet(parent, mode, rememberContext);
	}

	public void sort() {
		//		long start = System.currentTimeMillis();
		if (isSorted)
			return;
		Part part;
		size = 0;
		for (Iterator i = map.valueIterator(); i.hasNext();) {
			part = (Part) i.next();
			part.sort();
			size += part.removeDuplicates();
		}
		isSorted = true;
		isInDocumentOrder = false;
		//		System.out.println("sort took " + (System.currentTimeMillis() -
		// start) + "ms.");
	}

	public final void sortInDocumentOrder() {
		//		long start = System.currentTimeMillis();
		if (isInDocumentOrder)
			return;
		Part part;
		size = 0;
		for (Iterator i = map.valueIterator(); i.hasNext();) {
			part = (Part) i.next();
			part.sortInDocumentOrder();
			size += part.removeDuplicates();
		}
		isSorted = false;
		isInDocumentOrder = true;
		//		System.out.println("in-document-order sort took " +
		// (System.currentTimeMillis() - start) + "ms.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.xpath.value.AbstractSequence#setSelfAsContext()
	 */
	public void setSelfAsContext() {
		Part part;
		for (Iterator i = map.valueIterator(); i.hasNext();) {
			part = (Part) i.next();
			part.setSelfAsContext();
		}
	}

	public NodeProxy parentWithChild(DocumentImpl doc, long gid,
										boolean directParent,
										boolean includeSelf, int level) {
		Part part = getPart(doc, false, -1);
		return part == null ? null : part.parentWithChild(gid, directParent, includeSelf, level);
	}

	public DocumentSet getDocumentSet() {
		if (!isSorted)
			size = 0;
		Part part;
		DocumentSet ds = new DocumentSet();
		DocumentImpl doc, last = null;
		for (Iterator i = map.valueIterator(); i.hasNext();) {
			part = (Part) i.next();
			if (!isSorted) {
				part.sort();
				size += part.removeDuplicates();
			}
			for (int j = 0; j < part.length; j++) {
				doc = part.array[j].doc;
				if (last == null || last.docId != doc.docId)
					ds.add(part.array[j].doc);
				last = doc;
			}
		}
		isSorted = true;
		return ds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.AbstractNodeSet#hasChanged(int)
	 */
	public boolean hasChanged(int previousState) {
		return state != previousState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.exist.dom.AbstractNodeSet#getState()
	 */
	public int getState() {
		return state;
	}
	private static class Part {

		NodeProxy array[];
		DocumentImpl doc;
		int length = 0;

		Part(int initialSize, DocumentImpl myDoc) {
			array = new NodeProxy[initialSize];
			doc = myDoc;
		}

		void add(NodeProxy p) {
			// just check if this node has already been added. We only
			// check the last entry, which should avoid most of the likely
			// duplicates. The remaining duplicates are removed by
			// removeDuplicates().
			if (length > 0 && array[length - 1].gid == p.gid) {
				return;
			}
			if (length == array.length) {
				//int newLength = (length * 3)/2 + 1;
				final int newLength = length << 1;
				NodeProxy temp[] = new NodeProxy[newLength];
				System.arraycopy(array, 0, temp, 0, length);
				array = temp;
			}
			array[length++] = p;
		}

		boolean contains(long gid) {
			return get(gid) != null;
		}

		NodeProxy get(int pos) {
			return array[pos];
		}

		NodeProxy get(long gid) {
			int low = 0;
			int high = length - 1;
			int mid;
			NodeProxy p;
			while (low <= high) {
				mid = (low + high) / 2;
				p = array[mid];
				if (p.gid == gid)
					return p;
				if (p.gid > gid)
					high = mid - 1;
				else
					low = mid + 1;
			}
			return null;
		}

		void sort() {
			FastQSort.sortByNodeId(array, 0, length - 1);
		}

		void sortInDocumentOrder() {
			FastQSort.sort(array, new DocumentOrderComparator(), 0, length - 1);
		}

		/**
		 * Check if the node identified by its node id has an ancestor
		 * contained in this node set and return the ancestor found.
		 * 
		 * If directParent is true, only immediate ancestors (parents) are
		 * considered. Otherwise the method will call itself recursively for
		 * all the node's parents.
		 * 
		 * If includeSelf is true, the method returns also true if the node
		 * itself is contained in the node set.
		 */
		public NodeProxy parentWithChild(long gid, boolean directParent,
											boolean includeSelf, int level) {
			NodeProxy temp;
			if (includeSelf && (temp = get(gid)) != null)
				return temp;
			if (level < 0)
				level = doc.getTreeLevel(gid);
			while (gid > 0) {
				gid = XMLUtil.getParentId(doc, gid, level);
				if ((temp = get(gid)) != null)
					return temp;
				else if (directParent)
					return null;
				else
					--level;
			}
			return null;
		}

		/**
		 * Find all nodes in the current set being children of the specified
		 * parent.
		 * 
		 * @param parent
		 * @param mode
		 * @param rememberContext
		 * @return
		 */
		NodeSet getChildrenInSet(NodeProxy parent, int mode,
									boolean rememberContext) {
			NodeSet result = new ExtArrayNodeSet();
			// get the range of node ids reserved for children of the parent
			// node
			Range range = XMLUtil.getChildRange(parent.doc, parent.gid);
			int low = 0;
			int high = length - 1;
			int mid = 0;
			NodeProxy p;
			// do a binary search to pick some node in the range of valid child
			// ids
			while (low <= high) {
				mid = (low + high) / 2;
				p = array[mid];
				if (range.inRange(p.gid))
					break; // found a node, break out
				if (p.gid > range.getStart())
					high = mid - 1;
				else
					low = mid + 1;
			}
			if (low > high)
				return result; // no node found
			// find the first child node in the range
			while (mid > 0 && array[mid - 1].gid >= range.getStart())
				--mid;
			// walk through the range of child nodes we found
			for (int i = mid; i < length && array[i].gid <= range.getEnd(); i++) {
				switch (mode) {
					case NodeSet.DESCENDANT :
						if (rememberContext)
							array[i].addContextNode(parent);
						else
							array[i].copyContext(parent);
						result.add(array[i], range.getDistance());
						break;
					case NodeSet.ANCESTOR :
						if (rememberContext)
							parent.addContextNode(array[i]);
						else
							parent.copyContext(array[i]);
						result.add(parent, 1);
						break;
				}
			}
			return result;
		}

		NodeSet getRange(long lower, long upper) {
			NodeSet result = new ExtArrayNodeSet((int) (upper - lower) + 1);
			int low = 0;
			int high = length - 1;
			int mid = 0;
			NodeProxy p;
			// do a binary search to pick some node in the range of valid node
			// ids
			while (low <= high) {
				mid = (low + high) / 2;
				p = array[mid];
				if (p.gid >= lower && p.gid <= upper)
					break; // found a node, break out
				if (p.gid > lower)
					high = mid - 1;
				else
					low = mid + 1;
			}
			if (low > high)
				return result; // no node found
			// find the first child node in the range
			while (mid > 0 && array[mid - 1].gid >= lower)
				--mid;
			for (int i = mid; i < length && array[i].gid <= upper; i++) {
				result.add(array[i]);
			}
			return result;
		}

		void remove(NodeProxy node) {
			int low = 0;
			int high = length - 1;
			int mid = -1;
			NodeProxy p;
			while (low <= high) {
				mid = (low + high) / 2;
				p = array[mid];
				if (p.gid == node.gid)
					break;
				if (p.gid > node.gid)
					high = mid - 1;
				else
					low = mid + 1;
			}
			if (low > high)
				return; // not found
			if (mid < length - 1)
				System.arraycopy(array, mid + 1, array, mid, length - mid - 1);
			--length;
		}

		/**
		 * Remove all duplicate nodes from this part.
		 * 
		 * @return the new length of the part, after removing all duplicates
		 */
		int removeDuplicates() {
			int j = 0;
			for (int i = 1; i < length; i++) {
				if (array[i].gid != array[j].gid) {
					if (i != ++j)
						array[j] = array[i];
				}
			}
			length = ++j;
			return length;
		}

		final void setSelfAsContext() {
			for (int i = 0; i < length; i++) {
				array[i].addContextNode(array[i]);
			}
		}
	}
	private static class ExtArrayIterator implements Iterator, SequenceIterator {

		Iterator docsIterator;
		Part currentPart = null;
		int pos = 0;
		NodeProxy next = null;

		ExtArrayIterator(Int2ObjectHashMap map) {
			docsIterator = map.valueIterator();
			if (docsIterator.hasNext())
				currentPart = (Part) docsIterator.next();
			if (currentPart != null && currentPart.length > 0)
				next = currentPart.get(0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return next != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (next == null)
				return null;
			NodeProxy n = next;
			next = null;
			if (++pos == currentPart.length) {
				if (docsIterator.hasNext()) {
					currentPart = (Part) docsIterator.next();
					if (currentPart != null && currentPart.length > 0) {
						next = currentPart.get(0);
						pos = 0;
					}
				}
			} else
				next = currentPart.get(pos);
			return n;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.exist.xpath.value.SequenceIterator#nextItem()
		 */
		public Item nextItem() {
			return (Item) next();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
		}
	}
}
