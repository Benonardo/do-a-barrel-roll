package entropy.doahackedroll.api.event.api;

import nl.enjarai.doahackedroll.config.Sensitivity;

public interface RollEntity {
    void doAHackedRoll$changeElytraLook(double pitch, double yaw, double roll, Sensitivity sensitivity, double mouseDelta);

    void doAHackedRoll$changeElytraLook(float pitch, float yaw, float roll);

    boolean doAHackedRoll$isRolling();

    void doAHackedRoll$setRolling(boolean rolling);

    float doAHackedRoll$getRoll();

    float doAHackedRoll$getRoll(float tickDelta);

    void doAHackedRoll$setRoll(float roll);
}
