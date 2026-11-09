package mcjty.ariente.compat;

import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.TheOneProbePlugin;

public class ArienteTOPPlugin implements TheOneProbePlugin {

    @Override
    public void register(ITheOneProbe probe) {
        new ArienteTOPCompatibility.GetTheOneProbe().apply(probe);
    }
}
