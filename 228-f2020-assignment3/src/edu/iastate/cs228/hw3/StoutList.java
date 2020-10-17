package edu.iastate.cs228.hw3;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
   */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private final Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;

  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0) throw new IllegalArgumentException();

    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head;
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head the dummy node at the beginning of the list
   * @param tail the dummy node at the end of the list
   * @param nodeSize the amount of elements able to be stored in any one node
   * @param size the total amount of elements currently stored in the list
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }

  /**
   * Returns the current amount of items held within the list.
   *
   * @return the size of the list
   * */
  @Override
  public int size() { return size; }

  /**
   * Adds the item provided at the end of the current list.
   *
   * @param item the item to be added to the list
   * @return true is the addition succeeded, false otherwise
   * @throws NullPointerException if the item is equal to null
   */
  @Override
  public boolean add(E item)
  {
    if (item == null) {
        throw new NullPointerException();
    }

    if (size == 0) {
        Node node = new Node();
        node.addItem(item);

        node.next = tail;
        node.previous = head;

        head.next = node;
        tail.previous = node;

        ++size;
        return true;
    }
    else if (tail.previous.count == nodeSize) {
        Node node = new Node();
        node.addItem(item);

        node.previous = tail.previous;
        node.next = tail;

        tail.previous.next = node;
        tail.previous = node;

        ++size;
        return true;
    }
    else if (tail.previous.count < nodeSize) {
        tail.previous.addItem(item);

        ++size;
        return true;
    }

    return false;
  }

  /**
   * The rules for adding an item at pos are as follows:
   *    -if the list is empty, create a new node and put item at offset 0
   *    -otherwise if offset = 0 and one of the following two cases occurs:
   *        -if n has a predecessor which has fewer than nodeSize elements (and is not the head), put item in n’s predecessor
   *        -if n is the tail node and n’s predecessor has nodeSize elements, create a new node and put item at offset 0
   *    -otherwise if there is space in node n, put item in node n at offset, shifting array elements as necessary
   *    -otherwise, perform a split operation: move the last nodeSize / 2 elements of node n into a new successor node n', and then:
   *        -if offset <= nodeSize / 2, put item in node n at offset off
   *        -if offset > nodeSize / 2, put item in node n' at offset (offset - nodeSize / 2)
   *
   * @param pos the position at which to place the item
   * @param item the item to be added to the list
   */
  @Override
  public void add(int pos, E item)
  {
      Node currentNode;
      int currentNodeInt = pos / nodeSize;
      int offset = pos % nodeSize;
      int count = 0;

      currentNode = head.next;
      while (currentNode.count != 0 && count < currentNodeInt){
          currentNode = currentNode.next;
          ++count;
      }

      if (currentNode.count == 0){
          Node node = new Node();
          currentNode = node;

          (tail.previous).next = node;
          node.next = tail;
          node.previous = tail.previous;
          tail.previous = node;
      }

      NodeInfo nodeInfo = new NodeInfo(currentNode, offset);
      nodeInfo.add(item);
  }

    /**
     * The rules for removing an element are:
     *      -if the node n containing X is the last node and has only one element, delete it
     *      -otherwise, if n is the last node (thus with two or more elements) , or if n has more than elements, remove
     *          X from n, shifting elements as necessary
     *      -otherwise (the node n must have at most elements), look at its successor n' (note that we don’t look at
     *          the predecessor of n) and perform a merge operation as follows:
     *          -if the successor node n' has more than elements, move the first element from n' to n (mini-merge)
     *          -if the successor node n' has or fewer elements, then move all elements from n' to n and delete n' (full merge)
     *
     * @param pos the position of the item to be removed
     * @return the item that was removed
     */
  @Override
  public E remove(int pos)
  {
      if(pos > size) { throw new IndexOutOfBoundsException(); }

      Node currentNode;

      currentNode = head.next;
      int count = currentNode.count;
      while (count < pos){
          currentNode = currentNode.next;
          count += currentNode.count;
      }

      int offset;
      if(currentNode.count == 0) {
          currentNode = currentNode.previous;
          offset = currentNode.count;
      }
      else {
          offset = pos - (count - currentNode.count);
      }

      if(offset > currentNode.count - 1) {
          currentNode = currentNode.next;
          offset = 0;
      }

      NodeInfo nodeInfo = new NodeInfo(currentNode, offset);

      return nodeInfo.remove();
  }

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */
  public void sort()
  {
	  ListComparator comp = new ListComparator();
	  StoutListIterator iterator = new StoutListIterator();

      Node currentNode = head.next;

      // Unchecked warning unavoidable.
	  E[] listData = (E[]) new Comparable[size];

	  int count = 0;
	  while(iterator.hasNext())
	  {
	      listData[count] = iterator.next();

	      if (!currentNode.equals(iterator.currentNode)){
              currentNode = currentNode.next;
              (currentNode.previous).previous = null;
              (currentNode.previous).next = null;
          }
	      ++count;
      }

	  head.next = tail;
	  tail.previous = head;
	  size = 0;

	  insertionSort(listData, comp);

      this.addAll(Arrays.asList(listData));
  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse() 
  {
      StoutListIterator iterator = new StoutListIterator();

      Node currentNode = head;

      // Unchecked warning unavoidable.
      E[] listData = (E[]) new Comparable[size];

      int count = 0;
      while(iterator.hasNext())
      {
          listData[count] = iterator.next();

          if (!currentNode.equals(iterator.currentNode)){
              currentNode = currentNode.next;
              (currentNode.previous).previous = null;
              (currentNode.previous).next = null;
          }
          ++count;
      }

      head.next = tail;
      tail.previous = head;
      size = 0;

      bubbleSort(listData);

      this.addAll(Arrays.asList(listData));
  }

  /**
   * Constructs a new StoutListIterator
   *
   * @return the StoutListIterator
   */
  @Override
  public Iterator<E> iterator() { return new StoutListIterator(); }

  /**
   * Constructs a new StoutListIterator
   *
   * @return the StoutListIterator
   */
  @Override
  public ListIterator<E> listIterator() { return new StoutListIterator(); }

  /**
   * Constructs a new StoutListIterator at the given index
   *
   * @param index the position at which to construct the new StoutListIterator
   * @return the StoutListIterator
   */
  @Override
  public ListIterator<E> listIterator(int index) { return new StoutListIterator(index); }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   */
  public String toStringInternal()
  {
    return toStringInternal(null);
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter
   *            an iterator for this list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }


  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node
  {
    /**
     * Array of actual data elements.
     */
    // Unchecked warning unavoidable.
    public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        return;
      }
      data[count++] = item;
      //useful for debugging
        //System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  return;
      }
        if (count - offset >= 0) System.arraycopy(data, offset, data, offset + 1, count - offset);
      ++count;
      data[offset] = item;
      //useful for debugging 
        //System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset the offset of the item to remove within the node
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null;
      --count;
    }
  }

  /**
   * Used to iterate through the list and determine if there is a next/previous element, what that element is,
   * and add, remove, or set the item at that position
   */
  private class StoutListIterator implements ListIterator<E>
  {
      /**
       * The current node selected by the iterator
       */
	Node currentNode;

	/**
     * The current offset within the currentNode
     */
	int currentIndex;

	/**
     * The overall position of the cursor
     */
	int currentPosition;

	/**
     * Checks whether or not next() or previous() have been called since the last add() or remove()
     */
	boolean nextOrPrev;

	/**
     * The last index returned by next() or previous()
     */
	int returnedIndex;

    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	currentNode = head;
        next();
    	currentIndex = 0;
    	currentPosition = 0;
        nextOrPrev = false;
        returnedIndex = -1;
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     *  the position of the cursor
     */
    public StoutListIterator(int pos)
    {
        currentPosition = pos - 1;
    	currentIndex = (pos % nodeSize) - 1;
        nextOrPrev = false;
        returnedIndex = -1;

        int currentNodeInt = pos / nodeSize;
        int count = 0;

        currentNode = head;
        next();
        while (currentNode.count != 0 && count < currentNodeInt){
            currentNode = currentNode.next;
            ++count;
        }

    }

    /**
     * Returns true if this list iterator has more elements when traversing the list in the forward direction.
     * (In other words, returns true if next() would return an element rather than throwing an exception.)
     *
     * @return true if the list iterator has more elements when traversing the list in the forward direction
     */
    @Override
    public boolean hasNext()
    {
        if (currentIndex < nodeSize - 1) {
            for (int i = currentIndex; i < nodeSize; ++i) {
                if (currentNode.data[i] != null){
                    return true;
                }
            }
        }
        return currentNode.next.count != 0;
    }

    /**
     * Returns the next element in the list and advances the cursor position. This method may be called repeatedly
     * to iterate through the list, or intermixed with calls to previous() to go back and forth. (Note that alternating
     * calls to next and previous will return the same element repeatedly.)
     *
     * @return the next element in the list
     * @throws NoSuchElementException if the iteration has no next element
     */
    @Override
    public E next()
    {
        if(currentNode.count == 0)
        {
            if (currentNode.next.count != 0) {
                currentNode = currentNode.next;
                currentIndex = 0;
                ++currentPosition;

                nextOrPrev = true;
                returnedIndex = 0;
                return currentNode.data[0];
            }
        }
        else if (currentIndex < nodeSize)
        {
            for (int i = currentIndex; i < nodeSize; ++i) {
                if (currentNode.data[i] != null){
                    ++currentPosition;
                    ++currentIndex;

                    nextOrPrev = true;
                    returnedIndex = i;
                    return currentNode.data[i];
                }
                ++currentPosition;
            }
        }
        if (currentNode.next.count != 0) {
            currentNode = currentNode.next;
            currentIndex = 1;
            ++currentPosition;

            nextOrPrev = true;
            returnedIndex = 0;
            return currentNode.data[0];
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns true if this list iterator has more elements when traversing the list in the reverse direction.
     * (In other words, returns true if previous() would return an element rather than throwing an exception.)
     *
     * @return true if the list iterator has more elements when traversing the list in the reverse direction
     */
    @Override
    public boolean hasPrevious() { return currentPosition > 0 || currentNode.previous.count != 0; }

    /**
     * Returns the previous element in the list and moves the cursor position backwards. This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with calls to next() to go back and forth.
     * (Note that alternating calls to next and previous will return the same element repeatedly.)
     *
     * @return the previous element in the list
     * @throws NoSuchElementException if the iteration has no previous element
     */
    @Override
    public E previous()
    {
        if (currentIndex > 0) {
            --currentPosition;
            --currentIndex;

            nextOrPrev = true;
            returnedIndex = currentPosition;
            return currentNode.data[currentIndex];
        }
        if (currentNode.previous.count != 0) {
            for (int i = nodeSize - 1; i > 0; --i) {
                if (currentNode.data[i] != null) {
                    --currentPosition;
                    --currentIndex;

                    currentNode = currentNode.previous;
                    currentIndex = 0;

                    nextOrPrev = true;
                    returnedIndex = currentPosition;
                    return currentNode.data[i];
                }
                --currentPosition;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the index of the element that would be returned by a subsequent call to next().
     * (Returns list size if the list iterator is at the end of the list.)
     *
     * @return the index of the element that would be returned by a subsequent call to next, or list size if the
     *            list iterator is at the end of the list
     */
    @Override
    public int nextIndex()
    {
        return currentPosition;
    }

    /**
     * Returns the index of the element that would be returned by a subsequent call to previous().
     * (Returns -1 if the list iterator is at the beginning of the list.)
     *
     * @return the index of the element that would be returned by a subsequent call to previous, or -1 if the
     *            list iterator is at the beginning of the list
     */
    @Override
    public int previousIndex()
    {
        return currentPosition - 1;
    }

    /**
     * Removes from the list the last element that was returned by next() or previous() (optional operation).
     * This call can only be made once per call to next or previous. It can be made only if add(E) has not been called
     * after the last call to next or previous.
     *
     * @throws IllegalStateException if neither next nor previous have been called, or remove or add have been called
     *              after the last call to next or previous
     */
    @Override
    public void remove()
    {
        if(!nextOrPrev) { throw new IllegalStateException(); }

        NodeInfo nodeInfo = new NodeInfo(currentNode, returnedIndex % 4);
        nodeInfo.remove();

        nextOrPrev = false;
    }

    /**
     * Replaces the last element returned by next() or previous() with the specified element (optional operation).
     * This call can be made only if neither remove() nor add(E) have been called after the last call to next or previous.
     *
     * @param e the element with which to replace the last element returned by next or previous
     * @throws IllegalArgumentException if some aspect of the specified element prevents it from being added to this list
     * @throws IllegalStateException if neither next nor previous have been called, or remove or add have been called
     *              after the last call to next or previous
     */
    @Override
    public void set(E e)
    {
        if(e == null) { throw new IllegalArgumentException(); }
        if(!nextOrPrev) { throw new IllegalStateException(); }

        currentNode.removeItem(returnedIndex);
        --returnedIndex;
        currentNode.addItem(returnedIndex, e);
    }

    /**
     * Inserts the specified element into the list (optional operation). The element is inserted immediately before
     * the element that would be returned by next(), if any, and after the element that would be returned by previous(),
     * if any. (If the list contains no elements, the new element becomes the sole element on the list.) The new
     * element is inserted before the implicit cursor: a subsequent call to next would be unaffected, and a subsequent
     * call to previous would return the new element. (This call increases by one the value that would be returned
     * by a call to nextIndex or previousIndex.)
     *
     * @param e the element to insert
     * @throws IllegalArgumentException if some aspect of this element prevents it from being added to this list
     */
    @Override
    public void add(E e)
    {
        if(e == null) { throw new IllegalArgumentException(); }

        NodeInfo nodeInfo = new NodeInfo(currentNode, returnedIndex % 4);
        nodeInfo.add(e);

        nextOrPrev = false;
    }

      // Other methods you may want to add or override that could possibly facilitate
    // other operations, for instance, addition, access to the previous element, etc.
    // 
    // ...
    // 
  }
  

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
      int i, j;
      E temp;

      for (i = 1; i < arr.length; i++)
      {
          temp = arr[i];
          j = i - 1;

          while (j >= 0 && comp.compare(arr[j], temp) > 0)
          {
              arr[j + 1] = arr[j];
              j = j - 1;
          }
          arr[j + 1] = temp;
      }
  }

  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private void bubbleSort(E[] arr)
  {
      int n = arr.length;
      for (int i = 0; i < n-1; i++)
      {
          for (int j = 0; j < n-i-1; j++)
          {
              if (arr[j].compareTo(arr[j+1]) < 0)
              {
                  E temp = arr[j];
                  arr[j] = arr[j+1];
                  arr[j+1] = temp;
              }
          }
      }
  }

    /**
     * The NodeInfo class is used to assist in adding and removing items from
     * the list. It stores a node and the index that the operation has to be
     * done on.
     */
  private class NodeInfo
  {
      /**
       * The node that contains the position that the operation has to be done on.
       */
      Node node;

      /**
       * The index of the position within the given node.
       */
      int offset;

      /**
       * Constructs a new NodeInfo with the given node and offset.
       *
       * @param node the node with the item
       * @param offset the index of the item within the node
       */
      NodeInfo(Node node, int offset)
      {
          this.node = node;
          this.offset = offset;
      }

      /**
       * Adds an item to the node at the offset given to the constructor.
       *
       * @param item the item to be added to the list
       */
      public void add(E item)
      {
          if (item == null) {
              throw new NullPointerException();
          }

          if (size == 0) {
              Node node = new Node();
              node.addItem(item);

              node.next = tail;
              node.previous = head;

              head.next = node;
              tail.previous = node;

              ++size;
              return;
          }

          if (node.count < nodeSize) {
              node.addItem(offset, item);
          }
          else {
              Node newNode = new Node();

              for (int i = nodeSize / 2; i < nodeSize; ++i) {
                  newNode.addItem(node.data[nodeSize / 2]);
                  node.removeItem(nodeSize / 2);
              }
              if (offset <= nodeSize / 2) {
                  node.addItem(offset, item);
              }
              else {
                  newNode.addItem(offset - (nodeSize / 2), item);
              }
              newNode.next = node.next;
              newNode.previous = node;
              (node.next).previous = newNode;
              node.next = newNode;
          }
          ++size;
      }

      /**
       * Removes the item at the offset given to the constructor.
       *
       * @return the item that was removed
       */
      public E remove()
      {
          E removed;

          if((node.next.count == 0 || node.count > nodeSize / 2)) {
              if(node.count == 1) {
                  removed = node.data[0];
                  node.removeItem(0);

                  node.previous.next = tail;
                  node.next.previous= node.previous;
                  node.next = null;
                  node.previous = null;
              }
              else {
                  removed = node.data[offset];
                  node.removeItem(offset);
              }

          }
          else {
              if(node.next.count > nodeSize / 2) {
                  removed = node.data[offset];
                  node.removeItem(offset);

                  node.addItem(node.count, node.next.data[0]);
                  node.next.removeItem(0);
              }
              else {
                  node.removeItem(offset);
                  removed = node.data[offset];

                  for(int i = 0; i < node.next.count; ++i) {
                      node.addItem(node.count, node.next.data[i]);
                  }

                  node.next.previous = null;
                  node.next = node.next.next;
                  node.next.previous.next = null;
                  node.next.previous = node;
              }
          }

          --size;
          return removed;
      }
  }

  /**
   * The comparator used to see if two Comparable<E> objects are equal.
   */
  private class ListComparator implements Comparator<E>
  {
      @Override
      public int compare(E o1, E o2)
      {
          return o1.compareTo(o2);
      }
  }
}