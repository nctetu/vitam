/*******************************************************************************
 * This file is part of Vitam Project.
 *
 * Copyright Vitam (2012, 2016)
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated
 * by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.common.database.builder.request.configuration;

/**
 * Main language definition
 *
 *
 */
public abstract class BuilderToken {

    /**
     * Default prefix for command
     */
    public static final String DEFAULT_PREFIX = "$";
    /**
     * Default prefix for command
     */
    public static final char DEFAULT_PREFIX_CHAR = '$';
    /**
     * Default hash prefix for command
     */
    private static final String DEFAULT_HASH_PREFIX = "#";

    public BuilderToken() {
        // Empty constructor
    }

    /**
     *For a Select :<br>
     *                { $roots: roots, $query : query, $filter : filter, $projection : projection } or [ roots, query,
     *                filter, projection ]<br>
     *                For an Update : <br>
     *                { $roots: roots, $query : query, $filter : multi, $action : action } or [ roots, query, multi,
     *                action ]<br>
     *                For an Insert : <br>
     *                { $roots: roots, $query : query, $filter : multi, $data : data } or [ roots, query, multi, data ]
     *                <br>
     *                For a Delete : <br>
     *                { $roots: roots, $query : query, $filter : multi } or [ roots, query, multi ]<br>
     *
     *                Select is in a subtree, by default next level (1), except if $exactdepth is set with a value
     *                (exact depth) or $depth with a relative value (+ or -, meaning leaves or parents, 0 for no limit
     *                in leaves depth) Only one of $exactdepth and $depth might be set. If both are set, only $depth
     *                will be kept. <br>
     *                { expression, $exactdepth : exactdepth, $depth : /- depth }
     * 
     */
    public static enum GLOBAL {

        /**
         * Roots part (departure of the request)
         */
        ROOTS("roots"),
        /**
         * Query part (where condition)
         */
        QUERY("query"),
        /**
         * Filter part (limit, order by, ... for Query, or isMulti for others)
         */
        FILTER("filter"),
        /**
         * Projection part (returned fields for Query)
         */
        PROJECTION("projection"),
        /**
         * Action part (in case of update)
         */
        ACTION("action"),
        /**
         * Data part (in case of insert)
         */
        DATA("data");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private GLOBAL(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }

    }


    /**
     * Query model
     *
     */
    public static enum QUERY {
        /**
         * All expressions are grouped by an AND operator (all shall be true)<br>
         * $and : [ expression1, expression2, ... ]
         */
        AND("and"),
        /**
         * All expressions are grouped by an NOT operator (all shall be false)<br>
         * $not : [ expression1, expression2, ... ]
         */
        NOT("not"),
        /**
         * All expressions are grouped by an OR operator (at least one shall be true) <br>
         * $or : [ expression1, expression2, ... ]
         */
        OR("or"),
        /**
         * Field named 'name' shall exist <br>
         * $exists : name
         */
        EXISTS("exists"),
        /**
         * Field named 'name' shall not exist (faster than $not : [ $exists : name ] ) <br>
         * $missing : name
         */
        MISSING("missing"),
        /**
         * Field named 'name' shall be empty or set to null <br>
         * $isNull : name
         */
        ISNULL("isNull"),
        /**
         * field named 'name' contains at least one of the values 'value1', 'value2', ... <br>
         * $in : { name : [ value1, value2, ... ] }
         */
        IN("in"),
        /**
         * field named 'name' does not contain any of the values 'value1', 'value2', ... <br>
         * $nin : { name : [ value1, value2, ... ] }
         */
        NIN("nin"),
        /**
         * Size of an array named 'name' equals to specified length<br>
         * $size : { name : length }
         */
        SIZE("size"),
        /**
         * Comparison operator <br>
         * $gt : { name : value }
         */
        GT("gt"),
        /**
         * Comparison operator <br>
         * $lt : { name : value }
         */
        LT("lt"),
        /**
         * Comparison operator $gte <br>
         * : { name : value }
         */
        GTE("gte"),
        /**
         * Comparison operator <br>
         * $lte : { name : value }
         */
        LTE("lte"),
        /**
         * Comparison operator <br>
         * $ne : { name : value }
         */
        NE("ne"),
        /**
         * Comparison operator <br>
         * $eq : { name : value }
         */
        EQ("eq"),
        /**
         * Optimization of comparison operator in a range <br>
         * $range : { name : { $gte : value, $lte : value } }
         */
        RANGE("range"),
        /**
         * type might be Point (simple lng, lta), Box, Polygon <br>
         * $geometry : { $type : "type", $coordinates : [ [ lng1, lta1 ], [ lng2, lta2 ], ... ] }
         */
        GEOMETRY("geometry"),
        /**
         * $box : [ [ lng1, lta1 ], [ lng2, lta2 ] ]
         */
        BOX("box"),
        /**
         * $polygon : [ [ lng1, lta1 ], [ lng2, lta2 ], ... ]
         */
        POLYGON("polygon"),
        /**
         * $center : [ [ lng1, lta1 ], radius ]
         */
        CENTER("center"),
        /**
         * Selects geometries within a bounding geometry <br>
         * $geoWithin : { name : { geometry|box|polygon|center } }
         */
        GEOWITHIN("geoWithin"),
        /**
         * Selects geometries that intersect with a geometry <br>
         * $geoIntersects : { name : { geometry|box|polygon|center } }
         */
        GEOINTERSECTS("geoIntersects"),
        /**
         * Selects geometries in proximity to a point <br>
         * $near : { name : { geometry_point|[ lng1, lta1], $maxDistance : distance } }
         */
        NEAR("near"),
        /**
         * Selects where field named 'name' matches some words <br>
         * $match : { name : words, $max_expansions : n }
         */
        MATCH("match"),
        /**
         * Selects where field named 'name' matches a phrase (somewhere)<br>
         * $match_phrase : { name : phrase, $max_expansions : n }
         */
        MATCH_PHRASE("match_phrase"),
        /**
         * Selects where field named 'name' matches a phrase as a prefix of the field <br>
         * $match_phrase_prefix : { name : phrase, $max_expansions : n }
         */
        MATCH_PHRASE_PREFIX("match_phrase_prefix"),
        /**
         * Selects where field named 'name' matches a phrase as a prefix of the field <br>
         * $prefix : { name : phrase } <br>
         * Should not be used externally (but possible) but in replacement of match_phrase_prefix if parameter not
         * analyzed
         */
        PREFIX("prefix"),
        /**
         * Selects where fields named 'name' are like the one provided, introducing some "fuzzy", which tends to be
         * slower than mlt<br>
         * $flt : { $fields : [ name1, name2 ], $like : like_text }
         */
        FLT("flt"),
        /**
         * Selects where fields named 'name' are like the one provided <br>
         * $mlt : { $fields : [ name1, name2 ], $like : like_text }
         */
        MLT("mlt"),
        /**
         * Selects where field named 'name' contains something relevant to the search parameter. This search parameter
         * can contain wildcards (* ?), specifications:<br>
         * (x y) meaning x or y<br>
         * "x y" meaning exactly sub phrase "x y"<br>
         * +x -y meaning x must be present, y must be absent<br>
         * $search : { name : searchParameter }
         */
        SEARCH("search"),
        /**
         * Selects where field named 'name' contains a value valid with the corresponding regular expression. <br>
         * $regex : { name : regex }
         */
        REGEX("regex"),
        /**
         * Selects where field named 'name' contains exactly this term (lowercase only, no blank). Useful in simple
         * value field to find one specific item, or for multiple tests at once (AND implicit). <br>
         * $term : { name : term, name : term }
         */
        TERM("term"),
        /**
         * Selects where field named 'name' contains exactly this term (lowercase only, no blank) with optional wildcard
         * character (* and ?). Useful in simple value field to find one specific item.<br>
         * $wildcard : { name : term }
         */
        WILDCARD("wildcard"),
        /**
         * Selects a node by its exact path (succession of ids) <br>
         * $path : [ id1, id2, ... ]
         */
        PATH("path");


        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private QUERY(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }

    }

    /**
     * SelectFilter model
     *
     */
    public static enum SELECTFILTER {
        /**
         * Limit the elements returned to the nth first elements $limit : n
         */
        LIMIT("limit"),
        /**
         * According to an orderby, start to return the elements from rank start<br>
         * $offset : start
         */
        OFFSET("offset"),
        /**
         * Specify an orderby to respect in the return of the elements according to one field named 'name' and an
         * orderby ascendant (+1) or descendant (-1) <br>
         * $orderby : [ { key : +/-1 } ]
         */
        ORDERBY("orderby"),
        /**
         * Allows to specify some hints to the request server: cache/nocache<br>
         * $hint : [ cache/nocache, ... ]
         */
        HINT("hint");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private SELECTFILTER(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     * Projection model
     *
     */
    public static enum PROJECTION {
        /**
         * Specify the fields to return $fields : {name1 : 0/1, name2 : 0/1, ...}
         */
        FIELDS("fields"),
        /**
         * UsageContract reference that will be used to select the binary object version to return $usage : contractId
         */
        USAGE("usage");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private PROJECTION(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     * Arguments to some QUERY commands
     *
     */
    public static enum QUERYARGS {
        /**
         * Used in geometric queries
         */
        TYPE("type"),
        /**
         * Used in geometric queries
         */
        COORDINATES("coordinates"),
        /**
         * Used in geometric queries
         */
        MAXDISTANCE("maxDistance"),
        /**
         * Used in MLT queries
         */
        LIKE("like"),
        /**
         * Used in MLT queries
         */
        FIELDS("fields"),
        /**
         * Used in Match request
         */
        MAX_EXPANSIONS("max_expansions"),
        /**
         * Used in Set Depth (exact) part of each request
         */
        EXACTDEPTH("exactdepth"),
        /**
         * Used in Set Depth (relative) part of each request
         */
        DEPTH("depth"),
        /**
         * Used to specify that argument is a date
         */
        DATE("date");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private QUERYARGS(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     *
     * Range args model
     *
     */
    public static enum RANGEARGS {
        /**
         * Comparison operator $gt : value
         */
        GT("gt"),
        /**
         * Comparison operator $lt : value
         */
        LT("lt"),
        /**
         * Comparison operator $gte : value
         */
        GTE("gte"),
        /**
         * Comparison operator $lte : value
         */
        LTE("lte");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private RANGEARGS(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     * specific fields: nbunits, dua, ... <br>
     * $fields : [ #nbunits:1, #dua:1, #all:1... ]
     *
     * #all:1 means all, while #all:0 means none
     */
    public static enum PROJECTIONARGS {
        /**
         * Id of the item
         */
        ID("id"),
        /**
         * Number of units from each result (Unit = subUnit, ObjectGroup = objects)
         */
        NBUNITS("nbunits"),
        /**
         * All Dua for the result
         */
        DUA("dua"),
        /**
         * All fields for the result or None except Id
         */
        ALL("all"),
        /**
         * Object size
         */
        SIZE("size"),
        /**
         * Object format
         */
        FORMAT("format"),
        /**
         * Unit/ObjectGroup type
         */
        TYPE("type");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_HASH_PREFIX before the exactToken (#+exactToken)
         */
        private PROJECTIONARGS(String realName) {
            exactToken = DEFAULT_HASH_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }

    }

    /**
     * Specific values for Filter arguments
     *
     */
    public static enum FILTERARGS {
        /**
         * Cache could be used for this request
         */
        CACHE("cache"),
        /**
         * Cache should not be used for this request
         */
        NOCACHE("nocache"),
        /**
         * Query should not have a timeout (scrolling)
         */
        NOTIMEOUT("notimeout"),
        
        /**
         * Query concerns Others
         */
        OTHERS("others"),
        /**
         * Query concerns Units
         */
        UNITS("units"),
        /**
         * Query concerns ObjectGroups
         */
        OBJECTGROUPS("objectgroups");

        private final String exactToken;

        /**
         * Constructor
         */
        private FILTERARGS(String realName) {
            exactToken = realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     *
     * Update model
     *
     * <pre>
     * {\@code
     *
     * Pour mettre à jour (update) :
     * multiple update : { $multi : true/false } pour autoriser une mise à jour de plusieurs noeuds ou pas (si la requête présente plusieurs résultats mais $multi = false is erreur )
     * action :
     *   { $set : { clef : valeur, clef : valeur, ... } } / { $unset : { clef : "", ... } } positionne ou ajoute (si elle n'existait pas) une valeur pour une clef ou efface la clef
     *   { $inc : { clef : valeur, clef : valeur, ... } } incrémente la valeur
     *   { $rename : { clef : nouvelleclef, ... } } renomme une clef
     *   { $push : { clef : valeur, ... } : ajoute une valeur à une clef de liste (si la liste maliste est [ a, b, c], $push : { maliste : b } donnera maliste = [ a, b, c, b])
     *     { $push : { clef : { $each : [valeur, valeur, ... ] } } } idem mais plusieurs éléments en une fois ($push : { maliste : { $each : [ b, d, e, a] } } donnera  maliste = [ a, b, c, b, d, e, a] )
     *   { $add : { clef : valeur, ... } : ajoute une valeur à une clef de liste mais si celle-ci n'y est pas déjà (si la liste maliste est [ a, b, c], $add : { maliste : b } ne changera pas la liste, tandis que $add : { maliste : d } donnera maliste = [ a, b, c, d] )
     *     Si valeur est multiple (une liste) et que chacune des valeurs doit être intégrées : $add : { maliste : { $each : [ b, d, e, a] } } donnera maliste = [ a, b, c, d, e]
     *   { $pop : { clef : 1 ou -1 } } retire le dernier (1) ou le premier (-1) élément de la liste
     *   { $pull : { clef : valeur } } retire l'élément valeur de la liste
     *     { $pull : { clef : { $each : [valeur, valeur, ... ] } } } idem mais plusieurs éléments en une fois
     *   { $sort : { clef : 1 ou -1 } } pour trier une liste selon un ordre ascendant (1) ou descendant (-1)
     * 3 parties : critères de sélection (expression depuis root) + filtres (multi) + action
     * retourne : la requête + des info générales (nb de résultats) + les n premiers résultats (noeuds mis à jour) selon le format souhaité
     *
     * }
     * </pre>
     */
    public static enum UPDATEACTION {
        /**
         * $set : { name : value, name : value, ... }
         */
        SET("set"),
        /**
         * $unset : [ name, name, ... ]
         */
        UNSET("unset"),
        /**
         * increment one field named 'name' with default 1 or value <br>
         * $inc : { name : value }
         */
        INC("inc"),
        /**
         * set one field named 'name' with minimum value of current value and given value <br>
         * $min : { name : value }
         */
        MIN("min"),
        /**
         * set one field named 'name' with maximum value of current value and given value <br>
         * $max : { name : value }
         */
        MAX("max"),
        /**
         * rename one field named 'name' to 'newname' <br>
         * $rename : { name : newname }
         */
        RENAME("rename"),
        /**
         * Add one element at the end of a list value, or each element of a list if $each parameter is used <br>
         * $push : { name : { $each : [ value, value, ... ] } }
         */
        PUSH("push"),
        /**
         * Remove one specific element from a list or each element of a list if $each parameter is used <br>
         * $pull : { name : { $each : [ value, value, ... ] } }
         */
        PULL("pull"),
        /**
         * Add one element (or each element of a list) if not already in the list <br>
         * $add : { name : { $each : [ value, value, ... ] } }
         */
        ADD("add"),
        /**
         * Remove n element from a list from the end (1) or the beginning (-1)<br>
         * $pop : { name : -1/1 }
         */
        POP("pop");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private UPDATEACTION(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     * Update Args model
     *
     */
    public static enum UPDATEACTIONARGS {
        /**
         * Update argument
         */
        EACH("each");
        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private UPDATEACTIONARGS(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }

    /**
     * Action Filter model
     *
     */
    public static enum MULTIFILTER {
        /**
         * True to allow multiple update if multiple elements are found through the QUERY, else False will return an
         * error if multiple elements are found. $mult : true/false
         */
        MULT("mult");

        private final String exactToken;

        /**
         * Constructor Add DEFAULT_PREFIX before the exactToken ($+exactToken)
         */
        private MULTIFILTER(String realName) {
            exactToken = DEFAULT_PREFIX + realName;
        }

        /**
         * @return the exact token
         */
        public final String exactToken() {
            return exactToken;
        }
    }
}
