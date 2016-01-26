/*
 * StrokeGroup.java
 *
 * Created on 8 March 2005, 13:32
 */

package gita;
 
import java.util.LinkedList;
import java.util.Collection;
import java.util.Iterator;
import java.lang.UnsupportedOperationException;

import zone.HTMLwriter;

/**
 * Class referencing group of strokes. Typically, <i>Stroke</i>s are maintained in a <i>PageData</i>
 * structure, to which the list kept here is references, not copies. Thus the stroke groups are non-exclusive
 * and non hierarchical.
 * <p>
 * When the page is stored as SVG data, the stroke group information is reflected as
 * SVG groups, which are hierarchically arranged. Hopefully this won't cause too many issues.
 *<p>
 * Meanings are assigned to groups of strokes via Symbol structures, which have their own infrastructure
 * for grouping strokes. The StrokeGroup class is not intended to be used in that context.
 * <p>
 * The main purpose for these groups is for grouping items of signature data. These are
 * processed by a different subsystem to the main parsing and interpretation engine, and require
 * very different calculations individually, and as a group.
 *
 * @see PageData
 * @see Stroke
 * @see StrokeGroupInfo
 * @author dak
 * @since you asked
 */
public class StrokeGroup implements Collection<Stroke>
{
    
    /**
     * the possible types of a StrokeGroup. This is used to indicate which particular
     * StrokeGroupInfo structure to allocate
     */
    public enum Type {
     /** a group of strokes in a signature used for training data */   SignatureTraining,
     /** a group of strokes in a signature used for verification in a document */   Signature,
     /** a non-specific stroke group */   Vanilla
    }
    
    /**
     * a new blank instance of StrokeGroup
     */
    public StrokeGroup()
    {
        init(Type.Vanilla, null);
    }

    /**
     * a new StrokeGroup of the given type
     */
    public StrokeGroup(Type gt)
    {
        init(gt, null);
    }
    
    /**
     * a new StrokeGroup of the given type and name
     */
    public StrokeGroup(Type gt, String nm)
    {
        init(gt, nm);
    }
    
    /**
     * initialise a new StrokeGroup of the given type and name
     * @param gt the type of the group
     * @param nm the name of the group
     */
    public void init(Type gt, String nm)
    {
        nStrokes = 0;
        id = nm;
        strokes = new LinkedList();
        type = gt;
        
    }

    /**
     * adds all strokes in a LinkedList that are enclosed by the bounding box of the given
     * enclosing stroke.
     *
     * @param strokes the list of strokes to add from
     * @param enclosingStroke the stroke which will enclose strokes of interest
     */
    public void AppendEnclosedStrokes(Stroke enclosingStroke, LinkedList<Stroke> strokes)
    {
        for (Stroke sj: strokes) {
            if (sj != enclosingStroke) {
                boolean intrsx = enclosingStroke.bounds.Intersects(sj.bounds);
                if (intrsx) {
                    switch (type) {
                        case SignatureTraining:
                        case Signature: {
                            sj.info = new SignatureStroke(sj);
                            break;
                        }
                        case Vanilla: {
                            break;
                        }
                    }
                    add(sj); 
                }
            }
        }
    }
    
    /**
     * returns the i-th stroke of this group
     * @param i the index of stroke to return
     * @return the i-th stroke of this group
     */
    public Stroke get(int i)
    {
        return strokes.get(i);
    }
    
    /**
     * returns the number of strokes in this group
     *  @return the number of strokes in this group
     */
    public int size()
    {
        return nStrokes;
    }
    
    /**
     * calls the feature calculation routine associated with the <i>info</i> structure, if any
     *
     * @param http HTMLwriter for diagnostic output
     * @throws Cow
     */
    public void CalculateFeatures(HTMLwriter http) throws Cow
    {
        if (info != null) {
            info.CalculateFeatures(http);
        }
    }
    
    /**
     * clears the list
     */
    public void clear()
    {
        nStrokes = 0;
        strokes.clear();
    }
    
    /**
     * Adds the given element to the tail of the list.
     *
     * @param o element whose presence in this collection is to be ensured.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException <tt>add</tt> is not supported by
     *         this collection.
     * @throws ClassCastException class of the specified element prevents it
     *         from being added to this collection.
     * @throws NullPointerException if the specified element is null and this
     *         collection does not support null elements.
     * @throws IllegalArgumentException some aspect of this element prevents
     *         it from being added to this collection.
     */
    public boolean add(Stroke o)
    {
        if (strokes.add(o)) {
            nStrokes++;
            return true;
        }
        return false;
    }
    
    /**
     * Adds all of the elements in the specified collection to this collection
     *
     * @param c elements to be inserted into this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException if this collection does not
     *         support the <tt>addAll</tt> method.
     * @throws ClassCastException if the class of an element of the specified
     * 	       collection prevents it from being added to this collection.
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this collection does not support null
     *         elements, or if the specified collection is <tt>null</tt>.
     * @throws IllegalArgumentException some aspect of an element of the
     *	       specified collection prevents it from being added to this
     *	       collection.
     */
    public boolean addAll(Collection<? extends Stroke> c)
    {
        throw new UnsupportedOperationException("StrokeGroup::addAll unimplemented");
    }
    
    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element.
     *
     * @param o element whose presence in this collection is to be tested.
     * @return <tt>true</tt> if this collection contains the specified
     *         element
     * @throws ClassCastException if the type of the specified element
     * 	       is incompatible with this collection (optional).
     * @throws NullPointerException if the specified element is null and this
     *         collection does not support null elements (optional).
     */
    public boolean contains(Object o)
    {
        return strokes.contains(o);
    }
    
    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection.
     *
     * @param  c collection to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains all of the elements
     *	       in the specified collection
     * @throws ClassCastException if the types of one or more elements
     *         in the specified collection are incompatible with this
     *         collection (optional).
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this collection does not support null
     *         elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     */
    public boolean containsAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("StrokeGroup::containsAll unimplemented");
    }
    
    /**
     * Compares the specified object with this collection for equality. Always returns false.
     *
     * @param o Object to be compared for equality with this collection.
     * @return <tt>true</tt> if the specified object is equal to this
     * collection
     */
    public boolean equals(Object o)
    {
        return strokes.equals(o);
    }
    
    /**
     * Returns the hash code value for this collection. Always returns 0.
     * @return the hash code value for this collection
     */
    public int hashCode()
    {
        return 0;
    }
    
    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public boolean isEmpty()
    {
        return strokes.isEmpty();
    }
    
    /**
     * Returns an iterator over the elements in this collection.
     * 
     * @return an <tt>Iterator</tt> over the elements in this collection
     */
    public Iterator<Stroke> iterator()
    {
        return strokes.iterator();
    }
    
    /**
     * Removes a single instance of the specified element from this collection.
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws ClassCastException if the type of the specified element
     * 	       is incompatible with this collection (optional).
     * @throws NullPointerException if the specified element is null and this
     *         collection does not support null elements (optional).
     * @throws UnsupportedOperationException remove is not supported by this
     *         collection.
     */
    public boolean remove(Object o)
    {
        if (strokes.remove(o)) {
            nStrokes--;
            return true;
        }
        return false;
    }
    
    /**
     * Removes all this collection's elements that are also contained in the specified collection
     *
     * @param c elements to be removed from this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> method
     * 	       is not supported by this collection.
     * @throws ClassCastException if the types of one or more elements
     *         in this collection are incompatible with the specified
     *         collection (optional).
     * @throws NullPointerException if this collection contains one or more
     *         null elements and the specified collection does not support
     *         null elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     * @see #remove(Object)
     * @see #contains(Object)
     */

    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("StrokeGroup::removeAll unimplemented");
    }
    
    /**
     * Retains only the elements in this collection that are contained in the specified collection
     *
     * @param c elements to be retained in this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     * 	       is not supported by this Collection.
     * @throws ClassCastException if the types of one or more elements
     *         in this collection are incompatible with the specified
     *         collection (optional).
     * @throws NullPointerException if this collection contains one or more
     *         null elements and the specified collection does not support null 
     *         elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     */
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("StrokeGroup::retainAll unimplemented");
    }
    
    /**
     * Returns an array containing all of the elements in this collection. 
     *
     * @return an array containing all of the elements in this collection
     */
    public Object[] toArray()
    {
        return strokes.toArray();
    }
    
    /**
     * Returns an array containing all of the elements in this collection; 
     *
     * @param a the array into which the elements of this collection are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return an array containing the elements of this collection
     * 
     * @throws ArrayStoreException the runtime type of the specified array is
     *         not a supertype of the runtime type of every element in this
     *         collection.
     * @throws NullPointerException if the specified array is <tt>null</tt>.
     */
    public <Stroke>Stroke[] toArray(Stroke[] a)
    {
        return strokes.toArray(a);
    }
    
    /** the type of this stroke group */
    Type                type;
    /** additional information required and or calculated by this group */
    StrokeGroupInfo     info = null;
    /** the string id of this group */
    String              id = null;
    /** the number of strokes in this group */
    int                 nStrokes = 0;
    /** a list of the strokes in this group */
    LinkedList<Stroke>  strokes = null;
}
