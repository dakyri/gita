/**
 * Gita is a java servlet for visual language processing of stroke data from a digital pen device. It is based on
 * the GIDA patent, held by yours truly.
 *<p>
 * This web server implementation stands in the opposite direction to my stand that the Anoto device should be
 * talking locally. Sorry. There are advantages though:
 *<ul>
 *<li>Ease of implementing an Anoto application server, which would be a web server component.
 *<li>Easy display of graphics that can be stored in a hand-editable xml format, <a href="http://www.w3.org/TR/SVG/">SVG</a>.
 *  <ul>
 *    <li>Adobe provides a neato plugin for displaying svg. This plugin can also encapsulate and
 *        display regular truetype fonts, if we ever need to go there. Funny that they don't use the
 *        abominable css fonting system given that they sent their people along to the committee that
 *        developed it, and they more or less own truetype. 
 *    <li>Because of XML's namespace extensibility, the displayable graphic files can also be used to store
 *        other information about the stroke data that gita processes, including its interpretations and
 *        prior calculations.
 *    <li>Even though SVG is basically an adobe product, it has now slipped into the wierd and wonderful world of w3c
 *        reccomendations.
 *  </ul>
 *<li>Java's automatic garbage collection. The parsing and processing would be garbage collection hell in c++
 *<li>Java provides lot's of nice high-level classes for processing strings and doing pattern matching.
 *</ul>
 *<p>
 * Gita takes its input in either of two formats, delivered either by GET or POST:
 * <ul>
 * <li>Anoto log file data. This is a form of raw dump of positional information provided by the
 *     anoto demo kit. These are delivered by the <i>scrounger</i> applet in the <i>client</i> package.
 * <li> SVG files, an XML format for graphics. An uploaded anoto log data file gets converted into
 *     an SVG format which can then be easily modified and re-used.
 * </ul>
 * <p>
 * Ideally, we would like to talk to any digital ink device. Perhaps some of the other pens, perhaps a
 * wacom tablet, or a tablet pc. That last would probably entail accessing tablet dll's via java
 * native calls, and is probably not as amenable to the current client-server architecture, but would
 * give access to useful facilities of the tablet (such as character and digit recognition).
 *
 * @author dak
 * @since you asked
 */
package gita;