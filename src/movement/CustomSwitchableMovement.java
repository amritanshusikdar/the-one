package movement;

import core.Coord;
import core.Settings;
import movement.map.MapRoute;

public class CustomSwitchableMovement
        extends MovementModel{

    private MapRouteMovement MRM;
    private ProhibitedPolygonRwp PPM;
    private MovementModel movementType;
    private int[] interfaceCoord;
    private Coord interfacePoint;

    //==========================================================================//
    // API
    //==========================================================================//
    public CustomSwitchableMovement( final Settings settings ) {
        super( settings );
        MRM = new MapRouteMovement(settings);
        PPM = new ProhibitedPolygonRwp(settings);
        this.interfaceCoord = settings.getCsvInts("interfacePoint", 2);
        this.interfacePoint = new Coord(this.interfaceCoord[0], this.interfaceCoord[1]);

        this.movementType = MRM;
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
    }
    //==========================================================================//

    @Override
    public Path getPath() {
        String movementName = this.host.changeMovement();

        if (movementName.equals("PPM") && this.movementType != this.PPM) {
            this.movementType = this.PPM;
            this.getInitialLocation();      // do we actually need it?
            return this.movementType.findPath(this.host.getLocation(), null, this.interfacePoint);
        }
        else if (movementName.equals("MRM") && this.movementType != this.MRM) {
            this.movementType = this.MRM;
            this.getInitialLocation();      // do we actually need it?
            return this.movementType.findPath(this.host.getLocation(), null, this.interfacePoint);
        }

        return this.movementType.getPath();
    }

    @Override
    public Coord getInitialLocation() {
        return this.movementType.getInitialLocation();
    }

    @Override
    public MovementModel replicate() {
        return new CustomSwitchableMovement(this);
    }
}
