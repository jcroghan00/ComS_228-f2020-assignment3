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

  @Override
  public int size() { return size; }
  
  @Override
  public boolean add(E item)
  {
    if (item == null) {
        throw new IllegalArgumentException();
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

  @Override
  public void add(int pos, E item)
  {
      if (item == null) {
          throw new IllegalArgumentException();
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

      if (currentNode.count < nodeSize) {
          currentNode.addItem(offset, item);
      }
      else {
          Node newNode = new Node();

          for (int i = nodeSize / 2; i < nodeSize; ++i) {
              newNode.addItem(currentNode.data[nodeSize / 2]);
              currentNode.removeItem(nodeSize / 2);
          }
          if (offset <= nodeSize / 2) {
              currentNode.addItem(offset, item);
          }
          else {
              newNode.addItem(offset - (nodeSize / 2), item);
          }
          newNode.next = currentNode.next;
          newNode.previous = currentNode;
          (currentNode.next).previous = newNode;
          currentNode.next = newNode;
      }
      ++size;
  }

  @Override
  public E remove(int pos)
  {
      Node currentNode;
      E removed;
      
      int currentNodeInt = pos / nodeSize;
      int offset = pos % nodeSize;

      int count = 0;
      currentNode = head.next;
      while (currentNode.count != 0 && count < currentNodeInt){
          currentNode = currentNode.next;
          ++count;
      }

      if(currentNode.count == 0) {
          currentNode = currentNode.previous;
          offset = currentNode.count - 1;
      }

      if((currentNode.next.count == 0 || currentNode.count > nodeSize / 2)) {
          if(currentNode.count == 1) {
              removed = currentNode.data[0];
              currentNode.removeItem(0);

              currentNode.previous.next = tail;
              currentNode.next.previous= currentNode.previous;
              currentNode.next = null;
              currentNode.previous = null;
          }
          else {
              removed = currentNode.data[offset];
              currentNode.removeItem(offset);
          }

      }
      else {
          if(currentNode.next.count > nodeSize / 2) {
              removed = currentNode.data[offset];
              currentNode.removeItem(offset);

              currentNode.addItem(count, currentNode.next.data[0]);
              currentNode.next.removeItem(0);
          }
          else {
              currentNode.removeItem(offset);
              removed = currentNode.data[offset];

              for(int i = 0; i < currentNode.next.count; ++i) {
                  currentNode.addItem(count, currentNode.next.data[i]);
              }

              currentNode.next.previous = null;
              currentNode.next = currentNode.next.next;
              currentNode.next.previous.next = null;
              currentNode.next.previous = currentNode;
          }
      }

      --size;
      return removed;
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
	  listComparator comp = new listComparator();
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

      bubbleSort(listData);

      this.addAll(Arrays.asList(listData));
  }
  
  @Override
  public Iterator<E> iterator() { return new StoutListIterator(); }

  @Override
  public ListIterator<E> listIterator() { return new StoutListIterator(); }

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
 
  private class StoutListIterator implements ListIterator<E>
  {
	Node currentNode;
	int currentIndex;
	int currentPosition;
    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	currentNode = head.next;
    	currentIndex = 0;
    	currentPosition = 0;
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     *  the position of the cursor
     */
    public StoutListIterator(int pos)
    {
        currentPosition = pos;
    	currentIndex = pos % nodeSize;
    }

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

    @Override
    public E next()
    {
        if (currentIndex < nodeSize)
        {
            for (int i = currentIndex; i < nodeSize; ++i) {
                if (currentNode.data[i] != null){
                    ++currentPosition;
                    ++currentIndex;
                    return currentNode.data[i];
                }
                ++currentPosition;
            }
        }
        if (currentNode.next.count != 0)
        {
            currentNode = currentNode.next;
            currentIndex = 1;
            ++currentPosition;

            return currentNode.data[0];
        }
        return null;
    }

    @Override
    public boolean hasPrevious() { return currentIndex > 0 || currentNode.previous.count != 0; }

    @Override
    public E previous()
    {
        if (currentIndex > 0) {
            --currentPosition;
            --currentIndex;
            return currentNode.data[currentIndex];
        }
        if (currentNode.previous.count != 0) {
            for (int i = nodeSize - 1; i > 0; --i) {
                if (currentNode.data[i] != null) {
                    --currentPosition;

                    --currentIndex;

                    currentNode = currentNode.previous;
                    currentIndex = 0;

                    return currentNode.data[i];
                }
                --currentPosition;
            }
        }
        return null;
    }

    @Override
    public int nextIndex()
    {
        StoutListIterator tempIterator = new StoutListIterator(currentPosition);
        tempIterator.next();
        return tempIterator.currentPosition;
    }

    @Override
    public int previousIndex()
    {
        StoutListIterator tempIterator = new StoutListIterator(currentPosition);
        tempIterator.previous();
        if (currentPosition != tempIterator.currentPosition) { return tempIterator.currentPosition; }
        else { return -1; }
    }

    @Override
    public void remove()
    {
        //TODO
    }

    @Override
    public void set(E e)
    {
        // TODO
    }

    @Override
    public void add(E e)
    {
        // TODO
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

  private class listComparator implements Comparator<E>
  {
      @Override
      public int compare(E o1, E o2) {
          return o1.compareTo(o2);
      }
  }
}