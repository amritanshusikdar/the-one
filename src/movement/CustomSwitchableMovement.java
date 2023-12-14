package movement;

import core.Coord;
import core.Settings;

public class CustomSwitchableMovement
        extends MovementModel{

    private MapRouteMovement MRM;
    private ProhibitedPolygonRwp PPM;
    private MovementModel movementType;
    private int[] interfaceCoord;
    private Coord interfacePoint;
    private Coord defaultLocation;
    private boolean switchToPPM;
    private boolean switchToMRM;
    private boolean isSwitchable = true;

    //==========================================================================//
    // API
    //==========================================================================//
    public CustomSwitchableMovement(final Settings settings ) {
        super( settings );
        MRM = new MapRouteMovement(settings);
        PPM = new ProhibitedPolygonRwp(settings);
        this.interfaceCoord = settings.getCsvInts("interfacePoint", 2);
        this.interfacePoint = new Coord(this.interfaceCoord[0], this.interfaceCoord[1]);

        this.movementType = MRM;
        this.defaultLocation = new Coord(0,0);
    }

    public CustomSwitchableMovement( final CustomSwitchableMovement other ) {
        // Copy constructor will be used when setting up nodes. Only one
        // prototype node instance in a group is created using the Settings
        // passing constructor, the rest are replicated from the prototype.
        super( other );
        this.MRM = other.MRM;
        this.PPM = other.PPM;
        this.movementType = other.movementType;
        this.interfacePoint = other.interfacePoint;
        this.defaultLocation = other.defaultLocation;
        this.switchToPPM = false;
        this.switchToMRM = false;
    }
    //==========================================================================//

    @Override
    public Path getPath() {
        String movementName = this.host.changeMovement();
        Path path = null;

        if (movementName.equals("PPM") && this.movementType != this.PPM && !this.switchToPPM) {
            this.switchToPPM = true;
            path = this.movementType.findPath(this.host.getLocation(), null, this.interfacePoint);
            path.setSpeed(1);
            return path;
        }
        else if (movementName.equals("MRM") && this.movementType != this.MRM && !this.switchToMRM) {
            this.switchToMRM = true;
            path = this.movementType.findPath(this.host.getLocation(), null, this.interfacePoint);
            path.setSpeed(1);
            return path;
        }

        if (this.switchToPPM) {
            this.PPM.lastWaypoint = this.host.getLocation().clone();
            this.movementType = this.PPM;

            this.isSwitchable = false;
            this.switchToPPM = false;

            this.getHost().setNewDestination(new Coord(1135, 120));
            path = this.host.getPath();
        }
        else if (this.switchToMRM) {
            this.movementType = this.MRM;
            this.switchToMRM = false;
        }

        if (path == null) {
            path = this.movementType.getPath();
        }

        return path;
    }

    @Override
    public Path findPath(Coord src, Coord cp, Coord dest) {
        return this.movementType.findPath(src, cp, dest);
    }

    @Override
    public Coord getInitialLocation() {
        return this.movementType.getInitialLocation();
    }

    @Override
    public MovementModel replicate() {
        return new CustomSwitchableMovement(this);
    }

    public boolean isSwitchable() {
        return this.isSwitchable;
    }
}
