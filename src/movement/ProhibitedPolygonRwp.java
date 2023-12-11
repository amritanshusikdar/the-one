package movement;

import core.Coord;
import core.Settings;
import core.SettingsError;
import input.WKTReader;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Random Waypoint Movement with a prohibited region where nodes may not move
 * into. The polygon is defined by a *closed* (same point as first and
 * last) path, represented as a list of {@code Coord}s.
 *
 * @author teemuk
 */
public class ProhibitedPolygonRwp
extends MovementModel {

  //==========================================================================//
  // Instance vars
  //==========================================================================//
  private List <Coord> polygon = Arrays.asList(
          new Coord( 500, 250 ),
          new Coord( 250, 500 ),
          new Coord( 500, 750 ),
          new Coord( 750, 500 ),
          new Coord( 500, 250 )
  );

  private Coord lastWaypoint;
  /** Inverted, i.e., only allow nodes to move inside the polygon. */
  private final boolean invert;

  //==========================================================================//


  //==========================================================================//
  // Settings
  //==========================================================================//
  /** {@code true} to confine nodes inside the polygon */
  public static final String INVERT_SETTING = "rwpInvert";
  public static final boolean INVERT_DEFAULT = false;
  public static final String POLYGON_FILE = "polygonFile";

  //==========================================================================//




  //==========================================================================//
  // Implementation
  //==========================================================================//
  @Override
  public Path getPath() {
    // Creates a new path from the previous waypoint to a new one.
    final Path p;
    p = new Path( super.generateSpeed() );
    p.addWaypoint( this.lastWaypoint.clone() );

    // Add only one point. An arbitrary number of Coords could be added to
    // the path here and the simulator will follow the full path before
    // asking for the next one.
    Coord c;
    do {
      c = this.randomCoord();
    } while ( pathIntersects( this.polygon, this.lastWaypoint, c ) );
    p.addWaypoint( c );

    this.lastWaypoint = c;
    return p;
  }

  @Override
  public Path findPath(Coord src, Coord checkpoint, Coord coords) {
    Path shortestPath = new Path();
    Coord srcCoord = src;
    Coord destCoord = coords;

    if (!isInside(this.polygon, coords) || (checkpoint != null && !isInside(this.polygon, checkpoint))) {  // if the target node is not in the polygon
      return new Path();
    }

    if (checkpoint != null) {
      shortestPath = getOptimizedPath(srcCoord, checkpoint);
      srcCoord = checkpoint;
    }
    if (!shortestPath.hasNext()) {
      shortestPath = getOptimizedPath(srcCoord, destCoord);
    } else {
      Path pathToEnd = getOptimizedPath(srcCoord, destCoord);
      pathToEnd.getNextWaypoint();
      while (pathToEnd.hasNext()) {
        shortestPath.addWaypoint(pathToEnd.getNextWaypoint());
      }
    }

    return shortestPath;
  }

  private Path getOptimizedPath(Coord srcCoord, Coord destCoord) {
    Path shortestPath;
    if (countIntersectedEdges(this.polygon, srcCoord, destCoord) == 0) {  // if the target node is in the polygon and directly reachable
      shortestPath = new Path();
      shortestPath.addWaypoint(srcCoord);
      shortestPath.addWaypoint(destCoord);
    } else {  // if the target node is in the polygon and is reachable through intermediaries
      int[] okNodes = {1, 2};
      DijkstraPathFinder f = new DijkstraPathFinder(okNodes);

      MapNode srcNode = new MapNode(srcCoord);
      srcNode.addType(1);
      MapNode destNode = new MapNode(destCoord);
      destNode.addType(1);

      MapNode n1;
      MapNode n2;
      ArrayList<Coord> seen;

      ArrayList<MapNode> allNodes = new ArrayList<MapNode>();
      for (Coord c1 : this.polygon) {
        n1 = new MapNode(c1);
        n1.addType(1);
        allNodes.add(n1);

        if (countIntersectedEdges(this.polygon, srcCoord, c1) <= 2) {
          srcNode.addNeighbor(n1);
          n1.addNeighbor(srcNode);
        }
        if (countIntersectedEdges(this.polygon, destCoord, c1) <= 2) {
          destNode.addNeighbor(n1);
          n1.addNeighbor(destNode);
        }

        seen = new ArrayList<Coord>();
        for (Coord c2 : this.polygon) {
          if (!seen.contains(c2) && !c2.equals(c1)) {
            seen.add(c2);
            n2 = new MapNode(c2);
            n2.addType(1);
            allNodes.add(n2);

            if (countIntersectedEdges(this.polygon, c1, c2) <= 2) {
              n1.addNeighbor(n2);
              n2.addNeighbor(n1);
            }
          }
        }
      }

      List<MapNode> shortestPathNodes = f.getShortestPath(srcNode, destNode);

      shortestPath = new Path();
      for (MapNode n : shortestPathNodes) {
        shortestPath.addWaypoint(n.getLocation());
      }
    }

    return shortestPath;
  }

  @Override
  public Coord getInitialLocation() {
    do {
      this.lastWaypoint = this.randomCoord();
    } while ( ( this.invert ) ?
        isOutside( polygon, this.lastWaypoint ) :
        isInside( this.polygon, this.lastWaypoint ) );
    return this.lastWaypoint;
  }

  @Override
  public MovementModel replicate() {
    return new ProhibitedPolygonRwp( this );
  }

  private Coord randomCoord() {
    return new Coord(
        rng.nextDouble() * super.getMaxX(),
        rng.nextDouble() * super.getMaxY() );
  }
  //==========================================================================//


  //==========================================================================//
  // API
  //==========================================================================//
  public ProhibitedPolygonRwp( final Settings settings ) {
    super( settings );
    // Read the invert setting
    this.invert = settings.getBoolean( INVERT_SETTING, INVERT_DEFAULT );

    try {
      String fileName = settings.getSetting(POLYGON_FILE);
      this.polygon = readPoly(fileName);
    } catch (Throwable ignored) {}
  }

  public ProhibitedPolygonRwp( final ProhibitedPolygonRwp other ) {
    // Copy constructor will be used when setting up nodes. Only one
    // prototype node instance in a group is created using the Settings
    // passing constructor, the rest are replicated from the prototype.
    super( other );
    // Remember to copy any state defined in this class.
    this.invert = other.invert;
    this.polygon = other.polygon;
  }
  //==========================================================================//

  //==========================================================================//
  // Private - Utilities
  //==========================================================================//

  private static List<Coord> readPoly(String fileName) {
    List<Coord> polygon;
    WKTReader reader = new WKTReader();
    File polygonFile = null;

    try {
      polygonFile = new File(fileName);
      polygon = reader.readPoints(polygonFile);
    }
    catch (IOException ioe){
      throw new SettingsError("Couldn't read MapRoute-data file " +
              fileName + 	" (cause: " + ioe.getMessage() + ")");
    }

    return polygon;
  }

  //==========================================================================//


  //==========================================================================//
  // Private - geometry
  //==========================================================================//
  private static boolean pathIntersects(
      final List <Coord> polygon,
      final Coord start,
      final Coord end ) {
    final int count = countIntersectedEdges( polygon, start, end );
    return ( count > 0 );
  }

  private static boolean isInside(
      final List <Coord> polygon,
      final Coord point ) {
    final int count = countIntersectedEdges( polygon, point,
        new Coord( -10,0 ) );
    return ( ( count % 2 ) != 0 );
  }

  private static boolean isOutside(
      final List <Coord> polygon,
      final Coord point ) {
    return !isInside( polygon, point );
  }

  private static int countIntersectedEdges(
      final List <Coord> polygon,
      final Coord start,
      final Coord end ) {
    int count = 0;
    for ( int i = 0; i < polygon.size() - 1; i++ ) {
      final Coord polyP1 = polygon.get( i );
      final Coord polyP2 = polygon.get( i + 1 );

      final Coord intersection = intersection( start, end, polyP1, polyP2 );
      if ( intersection == null ) continue;

      if ( isOnSegment( polyP1, polyP2, intersection )
            && isOnSegment( start, end, intersection ) ) {
        count++;
      }
    }
    return count;
  }

  private static boolean isOnSegment(
      final Coord L0,
      final Coord L1,
      final Coord point ) {
    final double crossProduct
        = ( point.getY() - L0.getY() ) * ( L1.getX() - L0.getX() )
        - ( point.getX() - L0.getX() ) * ( L1.getY() - L0.getY() );
    if ( Math.abs( crossProduct ) > 0.0000001 ) return false;

    final double dotProduct
        = ( point.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
        + ( point.getY() - L0.getY() ) * ( L1.getY() - L0.getY() );
    if ( dotProduct < 0 ) return false;

    final double squaredLength
        = ( L1.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
        + (L1.getY() - L0.getY() ) * (L1.getY() - L0.getY() );
    if ( dotProduct > squaredLength + 1e-9 ) return false;

    return true;
  }

  private static Coord intersection(
      final Coord L0_p0,
      final Coord L0_p1,
      final Coord L1_p0,
      final Coord L1_p1 ) {
    final double[] p0 = getParams( L0_p0, L0_p1 );
    final double[] p1 = getParams( L1_p0, L1_p1 );
    final double D = p0[ 1 ] * p1[ 0 ] - p0[ 0 ] * p1[ 1 ];
    if ( D == 0.0 ) return null;

    final double x = ( p0[ 2 ] * p1[ 1 ] - p0[ 1 ] * p1[ 2 ] ) / D;
    final double y = ( p0[ 2 ] * p1[ 0 ] - p0[ 0 ] * p1[ 2 ] ) / D;

    return new Coord( x, y );
  }

  private static double[] getParams(
      final Coord c0,
      final Coord c1 ) {
    final double A = c0.getY() - c1.getY();
    final double B = c0.getX() - c1.getX();
    final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
    return new double[] { A, B, C };
  }
  //==========================================================================//
}
