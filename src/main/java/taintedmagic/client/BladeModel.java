package taintedmagic.client;

import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;

public final class BladeModel {
    public static ModelPart blade() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0, -2, 1, 48, 4, new CubeDeformation(-0.75F)), PartPose.offset(0, -40, 0));
        var grip = root.addOrReplaceChild("grip", CubeListBuilder.create().texOffs(0, 52).addBox(-2.5F, 0, -3.5F, 5, 1, 7), PartPose.offset(0, -40, 0));
        grip.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(22, 0).addBox(-1, -12, -1.5F, 2, 12, 3), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 64).bakeRoot();
    }
    public static ModelPart saya() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("saya", CubeListBuilder.create().texOffs(10, 0).addBox(-1, 0.5F, -2, 2, 48, 4), PartPose.offset(0, -40, 0));
        return LayerDefinition.create(mesh, 32, 64).bakeRoot();
    }
}
