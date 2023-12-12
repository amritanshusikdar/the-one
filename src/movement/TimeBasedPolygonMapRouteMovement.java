/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import core.*;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;

import java.util.Arrays;
import java.util.List;

/**
 * Map based movement model that uses predetermined paths within the map area.
 * Nodes using this model (can) stop on every route waypoint and find their
 * way to next waypoint using {@link DijkstraPathFinder}. There can be
 * different type of routes; see {@link #ROUTE_TYPE_S}.
 */
public class TimeBasedPolygonMapRouteMovement extends MapBasedMovement implements
	SwitchableMovement {

	/** Per node group setting used for selecting a route file ({@value}) */
	public static final String ROUTE_FILE_S = "routeFile";
	/**
	 * Per node group setting used for selecting a route's type ({@value}).
	 * Integer value from {@link MapRoute} class.
	 */
	public static final String ROUTE_TYPE_S = "routeType";

	/**
	 * Per node group setting for selecting which stop (counting from 0 from
	 * the start of the route) should be the first one. By default, or if a
	 * negative value is given, a random stop is selected.
	 */
	public static final String ROUTE_FIRST_STOP_S = "routeFirstStop";

	/** the Dijkstra shortest path finder */
	private DijkstraPathFinder pathFinder;

	/** Prototype's reference to all routes read for the group */
	private List<MapRoute> allRoutes = null;
	/** next route's index to give by prototype */
	private Integer nextRouteIndex = null;
	/** Index of the first stop for a group of nodes (or -1 for random) */
	private int firstStopIndex = -1;

	/** Route of the movement model's instance */
	private MapRoute route;

	/** From here on Added by Nurlan */
	/** {@code true} to confine nodes inside the polygon */
	public static final String INVERT_SETTING = "rwpInvert";
	public static final boolean INVERT_DEFAULT = false;

	/** Statically defined Polygon area */
	final List <Coord> polygon = Arrays.asList(
			new Coord( 500, 250 ),
			new Coord( 250, 500 ),
			new Coord( 500, 750 ),
			new Coord( 750, 500 ),
			new Coord( 500, 250 )
	);
	private Coord lastWaypoint;
	/** Inverted, i.e., only allow nodes to move inside the polygon. */
	private final boolean invert;

	/** Until here by Nurlan */

	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * @param settings The Settings object where the settings are read from
	 */
	public TimeBasedPolygonMapRouteMovement(Settings settings) {
		super(settings);

		/** From here on Added by Nurlan */
		this.invert = settings.getBoolean( INVERT_SETTING, INVERT_DEFAULT );
		/** Until here by Nurlan */

		String fileName = settings.getSetting(ROUTE_FILE_S);
		int type = settings.getInt(ROUTE_TYPE_S);
		allRoutes = MapRoute.readRoutes(fileName, type, getMap());
		nextRouteIndex = 0;
		pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
		this.route = this.allRoutes.get(this.nextRouteIndex).replicate();
		if (this.nextRouteIndex >= this.allRoutes.size()) {
			this.nextRouteIndex = 0;
		}

		if (settings.contains(ROUTE_FIRST_STOP_S)) {
			this.firstStopIndex = settings.getInt(ROUTE_FIRST_STOP_S);
			if (this.firstStopIndex >= this.route.getNrofStops()) {
				throw new SettingsError("Too high first stop's index (" +
						this.firstStopIndex + ") for route with only " +
						this.route.getNrofStops() + " stops");
			}
		}
	}

	/**
	 * Copyconstructor. Gives a route to the new movement model from the
	 * list of routes and randomizes the starting position.
	 * @param proto The MapRouteMovement prototype
	 */
	protected TimeBasedPolygonMapRouteMovement(TimeBasedPolygonMapRouteMovement proto) {
		super(proto);

		/** From here on Added by Nurlan */
		this.invert = proto.invert;
		/** Until here by Nurlan */

		this.route = proto.allRoutes.get(proto.nextRouteIndex).replicate();
		this.firstStopIndex = proto.firstStopIndex;

		if (firstStopIndex < 0) {
			/* set a random starting position on the route */
			this.route.setNextIndex(rng.nextInt(route.getNrofStops()-1));
		} else {
			/* use the one defined in the config file */
			this.route.setNextIndex(this.firstStopIndex);
		}

		this.pathFinder = proto.pathFinder;

		proto.nextRouteIndex++; // give routes in order
		if (proto.nextRouteIndex >= proto.allRoutes.size()) {
			proto.nextRouteIndex = 0;
		}
	}

	@Override
	public Path getPath() {
		double curTime = getSimCurrTime();
		if(curTime >= 0 && curTime <= 2000 ){ // Change here
			System.out.println("first");
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
			System.out.println(c);
			return p;
		}
		else if(curTime > 2000 && curTime <= 3000 ){ // Change here
			System.out.println("second");
			final Path p;
			p = new Path( super.generateSpeed() );
			p.addWaypoint( this.lastWaypoint.clone() );

			// Add only one point. An arbitrary number of Coords could be added to
			// the path here and the simulator will follow the full path before
			// asking for the next one.
			Coord c = new Coord(500, 250);
//			do {
//				c = this.randomCoord();
//			} while ( pathIntersects( this.polygon, this.lastWaypoint, c ) );
			p.addWaypoint( c );

			this.lastWaypoint = c;
			return p;
		}
		else if(curTime > 3000 && curTime <= 3500 ){ // Change here
			System.out.println("second");
			final Path p;
			p = new Path( super.generateSpeed() );
			p.addWaypoint( this.lastWaypoint.clone() );

			// Add only one point. An arbitrary number of Coords could be added to
			// the path here and the simulator will follow the full path before
			// asking for the next one.
			Coord c = new Coord(150, 200);
//			do {
//				c = this.randomCoord();
//			} while ( pathIntersects( this.polygon, this.lastWaypoint, c ) );
			p.addWaypoint( c );

			this.lastWaypoint = c;
			return p;
		}
		else {
			System.out.println("third");
			Path p = new Path(generateSpeed());
			MapNode to = route.nextStop();

			List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);

			// this assertion should never fire if the map is checked in read phase
			assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
					to + ". The simulation map isn't fully connected";

			for (MapNode node : nodePath) { // create a Path from the shortest path
				p.addWaypoint(node.getLocation());
			}

			lastMapNode = to;

			return p;
		}
	}

	/**
	 * Returns the first stop on the route
	 */
	@Override
	public Coord getInitialLocation() {
		if (lastMapNode == null) {
			lastMapNode = route.nextStop();
		}

		do {
			this.lastWaypoint = this.randomCoord();
		} while ( ( this.invert ) ?
				isOutside( polygon, this.lastWaypoint ) :
				isInside( this.polygon, this.lastWaypoint ) );
		return this.lastWaypoint;

//		return lastMapNode.getLocation().clone();
	}

	@Override
	public Coord getLastLocation() {
		if (lastMapNode != null) {
			return lastMapNode.getLocation().clone();
		} else {
			return null;
		}
	}


	@Override
	public TimeBasedPolygonMapRouteMovement replicate() {
		return new TimeBasedPolygonMapRouteMovement(this);
	}

	/**
	 * Returns the list of stops on the route
	 * @return The list of stops
	 */
	public List<MapNode> getStops() {
		return route.getStops();
	}

	/**
	 * From here on Added by Nurlan
	 **/
	/** functions for time Rwp*/
	protected double getSimEndTime() {
//		final double endTime = SimScenario.getInstance().getEndTime();
		return SimScenario.getInstance().getEndTime();
	}

	protected double getSimCurrTime() {
//		final double curTime = SimClock.getTime();
		return SimClock.getTime();
	}

	/** functions for polygon Rwp*/
	private Coord randomCoord() {
		return new Coord(
				rng.nextDouble() * super.getMaxX(),
				rng.nextDouble() * super.getMaxY() );
	}

	private static boolean pathIntersects(
			final List <Coord> polygon,
			final Coord start,
			final Coord end ) {
		final int count = countIntersectedEdges( polygon, start, end );
		return ( count > 0 );
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
		if ( dotProduct > squaredLength ) return false;

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

	/** Until here by Nurlan */
}
