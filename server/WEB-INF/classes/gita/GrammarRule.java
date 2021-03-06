/*
 * GrammarRule.java
 *
 * Created on 19 May 2005, 16:43
 *
 */

package gita;

import java.util.LinkedList;
import java.util.ArrayList;

/**
 * A rule to drive gita's visual language recognition system.
 *<p>
 * <i>Entity</i> objects are the fundamental units in
 * the gita rules. An entity represents either a single terminal of the visual language, a single
 * non-terminal of the visual language, or a relationship that must hold between grouped
 * terminals and non-terminals. A rule consists of a list of entities that constitue the terminals
 * and non-terminals associated with the rule, and a separated list of relationships.
 *
 * @see GitaGrammar
 * @author dak
 * @since you asked
 * @see Entity
 * @see Term
 * @see Nonterm
 * @see Relationship
 */
public class GrammarRule {
    
    /**
     * value for the type field of an entity. This encompasses the various relationships described in the
     * appendix of the GIDA patent, non-terminal symbols (ie references to other grammar rules) and
     * terminal symbols (interface strokes such as punctuation marks that are probabilistically recognised
     * outright)
     * 
     * @see Entity
     */
    enum Type {
/** an entity corresponding to a non-terminal in the rule */             NonTerminal,
/** an entity corresponding to a terminal in the rule */                 Terminal,
/** the "Group" relationship. entities are geometrically close */        Group,
/** the "Contains" relationship. an entity encloses others */            Contains,
/** the "Follow" relationship. an entity is immediately to the right or is at the start of a "line" below */        Follow,
/** the "Right" relationship. an entity immediately geometrically to the right of another */        Right,
/** the "Row" relationship. entities are geometrically in a row */        Row,
/** the "Column" relationship. entities are geometrically in a column */        Column,
/** the "Underline" relationship. the first entity in a group is a straight stroke the runs immediately underneath the other entities */        Underline,
/** the "Below" relationship. the first entity is geometrically below all the others */        Below,
/** the "Empty" relationship. this entity does not geometrically enclose any others */        Empty,
/** the "Sides" relationship. the entity is a polygon with a given number of sides. Takes additional int parameter, n. */        Sides,
/** the "Convex" relationship. the entity is a convex polygon */        Convex,
/** the "Straight" relationship. the entity is a graphic line that is geometrically straight */        Straight,
/** the "Horizontal" relationship. the entity is a straight line that runs horizontally */        Horizontal,
/** the "Vertical" relationship. the entity is a straight line that runs vertically */        Vertical,
/** the "Overlay" relationship. the entities bounding boxes substantially overlap */        Overlay,
/** the "Connect" relationship. true if the entities are geometrically connected */        Connect,
/** the "Endpoint" relationship. a pseudo relationship for establishing the base points of a connection */        Endpoint,
/** the "Branchpoint" relationship. a pseudo relationship for establishing the base points of a connection */        Branchpoint,
/** the "Headconnect" relationship. a pseudo relationship for establishing the base points of a connection */        Headconnect,
/** the "Tailconnect" relationship. a pseudo relationship for establishing the base points of a connection */        Tailconnect,
/** the "Angle" relationship. the entities meet at a given particular angle */        Angle,
/** the "Label" relationship. a pseudo relationship for establishing labeled constructs */        Label,
/** the "Width" relationship. entities have the same geometric width (x dimension)*/        Width,
/** the "Height" relationship. entities have the same geometric height (y dimension) */        Height,
/** the "Size" relationship. entities have the same geometric x and y dimension */        Size,
/** the "Angleconnect" relationship. true if entities connect at a particular angle */        Angleconnect,
/** the "Tail" relationship. pseudo relationship reversing the tail and head of a connecting entitiy */        Tail,
/** the "Fill" relationship. one entity encloses the others, which substantially cover the area of the enclosing one */        Fill,
/** the "Parallel" relationship. the entities are straight lines that are gometrically parallel */        Parallel,
/** the "Intersection" relationship. the entities intersect at a particular angle */        Intersection,
/** the "Cross" relationship. the entities are equal lengthed straight lines intersecting at a midpoint */        Cross,
/** the "Edgeconnect" relationship. the entities connect geometrically at particular edges */        Edgeconnect,
/** the "Vertexconnect" relationship. the entities connect geometrically at particular corners */        Vertexconnect,
/** the "Partition" relationship. one of the entitities partitions the other into regions */        Partition,
/** the "Compartment" relationship. the entities form a labeled sub area of an enclosing region */        Compartment
    }
    
    /**   
     * <i>Entity</i> objects are the fundamental units in
     * the gita rules. An entity represents either a single terminal of the visual language, a single
     * non-terminal of the visual language, or a relationship that must hold between grouped
     * terminals and non-terminals. A rule consists of a list of entities that constitue the terminals
     * and non-terminals associated with the rule, and a separated list of relationships.
     *<p>
     * Any entity, including a relationship, can appear in a relationship. The parent field
     * is a reference back to a the innermost relationship involving this entity. The parent
     * relationship may refer back to other relationships, and so on ...
     */
    public static class Entity
    {
        /**
         * creates a new instance of <i>Entity</i>
         */
        Entity(Type t)
        {
            type = t;
            parent = null;
        }
        /** the type of this entity */
        public Type             type;
        /** the parent relationship of this entity */
        public Relationship     parent;
    }
    
    /**
     * an extension of <i>Entity</i> for relationship references in a rule body.
     *<p>
     * this class may need to be extended to include non-entity parameters into a relationship,
     * such as angle, label, and tolerances. I could actually make a strong case here for
     * multiple inheritance for the attribute extensions... but I won't.
     */
    public static class Relationship extends Entity
    {
        /**
         * constructs a new instance of Relationship
         */
        Relationship(Type t)
        {
            super(t);
            params = new ArrayList();
        }
        
        public boolean add(Entity e)
        {
            if (params.add(e)) {
                e.parent = this;
                return true;
            }
            return false;
        }
        
        /** the entities involved in this relationship */
        public ArrayList<Entity>    params;
    }
    
    /**
     * an extension of <i>Relationship</i> for relationship references with a count sttribute
     *<p>
     * used for the Sides relationship
     */
    public static class CountRelationship extends Relationship
    {
        /**
         * constructs a new instance of Relationship
         */
        CountRelationship(Type t, int n)
        {
            super(t);
            count = n;
        }
        
        /** the integer count attribute */
        int             count;
    }
    
    /**
     * an extension of <i>Relationship</i> for relationship references in a rule body that contain angle attributes.
     *<p>
     * used by the Angle, Angleconnect, Intersection relationships.
     */
    public static class AngleRelationship extends Relationship
    {
        /**
         * constructs a new instance of AngleRelationship
         */
        AngleRelationship(Type t, float a, float to)
        {
            super(t);
            angle = a;
            tolerance = to;
        }
        
        /** the angle attribute */
        public float             angle;
        /** the tolerance attribute */
        public float             tolerance;
    }
    
    /**
     * an extension of <i>Relationship</i> for relationship references in a rule body that contain scale attributes.
     *<p>
     * used by the Width, Height, Size relationships.
     */
    public static class ScaleRelationship extends Relationship
    {
        /**
         * constructs a new instance of AngleRelationship
         */
        ScaleRelationship(Type t, float a, float to)
        {
            super(t);
            scale = a;
            tolerance = to;
        }
        
        /** the scale factor with which this comparison is made */
        public float             scale;
        /** the tolerance attribute */
        public float             tolerance;
    }
    
    /**
     * an extension of <i>Relationship</i> for relationship references in a rule body that contain tolerance attributes.
     *<p>
     * used with the Straight, Horizontal, Vertical, Overlay, Parallel relationships.
     */
    public static class TolRelationship extends Relationship
    {
        /**
         * constructs a new instance of TolRelationship
         */
        TolRelationship(Type t, float to)
        {
            super(t);
            tolerance = to;
        }
        
        /** the tolerance attribute */
        float             tolerance;
    }
    
    /**
     * an extension of <i>Relationship</i> for relationship references in a rule body that contain tolerance attributes.
     *<p>
     * used with the Partition, Label, Branchpoint, Endpoint relationships.
     */
    public static class LabelRelationship extends Relationship
    {
        /**
         * constructs a new instance of TolRelationship
         */
        LabelRelationship(Type t, String lbl)
        {
            super(t);
            label = lbl;
        }
        
        /** the label attribute */
        String             label;
    }

    /**
     * an extension of <i>Relationship</i> for relationship references in a rule body that contain partition index attributes.
     *<p>
     * used with the Compartment relationships.
     */
    public static class PartIndexRelationship extends Relationship
    {
        /**
         * constructs a new instance of TolRelationship
         */
        PartIndexRelationship(Type t, int ind)
        {
            super(t);
            index = ind;
        }
        
        /** the index attribute */
        int                 index;
    }

    /**
     * an extension of <i>Relationship</i> for relationship references in a rule body that contain partition index attributes.
     *<p>
     * used with the Partition relationships.
     */
    public static class EnclCnxRelationship extends Relationship
    {
        /**
         * constructs a new instance of TolRelationship
         */
        EnclCnxRelationship(Type t, int i1, int i2)
        {
            super(t);
            boxIndex = i1;
            tgtIndex = i2;
        }
        
        /** the index attribute for the box */
        int                 boxIndex;
        /** the index attribute for the target */
        int                 tgtIndex;
    }

    /**
     * an extension of <i>Entity</i> for non-terminal references in a rule body
     */
    public static class Term extends Entity
    {
        /**
         * creates a new instance of Term
         */
        Term(Symbol.Type symt)
        {
            super(Type.Terminal);
            symtype = symt;
        }
        /** the kind of non-terminal symbol of which this is a reference */
        public Symbol.Type     symtype;
    }
    
    /**
     * an extension of <i>Entity</i> for non-terminal references in a rule body
     */
    public static class Nonterm extends Entity
    {
        /**
         * creates a new instance of Nonterm
         */
        Nonterm(String tgt)
        {
            super(Type.NonTerminal);
            target = tgt;
        }
        /** the name of the target rule for this non-terminal entity */
        public String          target;
    }
    
    /**
     * Creates a new instance of GrammarRule
     */
    public GrammarRule(String nm, String tgt)
    {
        id = nm;
        target = tgt;
        body = new ArrayList();
        soul = new ArrayList();
    }
    
    public boolean add(Entity e)
    {
        return body.add(e);
    }
    
    public boolean add(Relationship e)
    {
        return soul.add(e);
    }
    
    /** string id of this rule, presumably unique */
    public String                  id;
    /** string name of the target of this rule */
    public String                  target;
    /** body of this rule, an ordered list of the entities that constitute the rule */
    public ArrayList<Entity>       body;
    /** the bits of this rule that are neither terminal or non-terminal ... relationships */
    public ArrayList<Relationship> soul;
}
