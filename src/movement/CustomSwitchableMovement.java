package movement;

import core.Coord;
import core.Settings;
import movement.map.MapRoute;

public class CustomSwitchableMovement
        extends MovementModel{

    private MapRouteMovement MRM;
    private ProhibitedPolygonRwp PPM;
    private MovementModel movementType;

    //==========================================================================//
    // API
    //==========================================================================//
    public CustomSwitchableMovement( final Settings settings ) {
        super( settings );
        MRM = new MapRouteMovement(settings);
        PPM = new ProhibitedPolygonRwp(settings);
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
    }
    //==========================================================================//

    @Override
    public Path getPath() {
        if (this.host.changeMovement()) {
            this.movementType = this.PPM;
            this.getInitialLocation();
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
