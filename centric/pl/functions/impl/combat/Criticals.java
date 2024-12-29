package centric.pl.functions.impl.combat;

import centric.pl.events.impl.EventPacket;
import centric.pl.functions.api.Category;
import centric.pl.functions.api.Function;
import centric.pl.functions.api.FunctionRegister;
import centric.pl.functions.settings.impl.ModeSetting;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;

@FunctionRegister(name = "Criticals", type = Category.Combat, beta = false)
public class Criticals extends Function {

        public static boolean cancelCrit;

        public final ModeSetting mode = new ModeSetting("Œ·ıÓ‰", "NCP", "NCP", "OldNCP", "NCPUpdate", "Grim", "Matrix", "FunTime");

        public Criticals() {
            addSettings(mode);
        }

        @Subscribe
        public void onPacket(EventPacket e) {
            if (e.isSend()) {
                if (e.getPacket() instanceof CUseEntityPacket packet) {
                    if (packet.getAction() == CUseEntityPacket.Action.ATTACK) {
                        Entity entity = packet.getEntityFromWorld(mc.world);
                        if (entity == null || entity instanceof EnderCrystalEntity || cancelCrit)
                            return;
                        sendCrit();
                    }
                }
            }
        }

        public void sendCrit() {
            if (mc.player == null || mc.world == null && !isState())
                return;
            if ((mc.player.isOnGround() || mc.player.abilities.isFlying || mode.is("Grim") && !mc.player.isInLava() && !mc.player.isInWater())) {
                if (mode.is("NCP")) {
                    critPacket(0.0625D, false);
                    critPacket(0., false);
                }
                if (mode.is("NCPUpdate")) {
                    critPacket(0.000000271875, false);
                    critPacket(0., false);
                }
                if (mode.is("OldNCP")) {
                    critPacket(0.00001058293536, false);
                    critPacket(0.00000916580235, false);
                    critPacket(0.00000010371854, false);
                }
                if (mode.is("Grim")) {
                    if (!mc.player.isOnGround())
                        critPacket(-0.000001, false);
                }
                if (mode.is("Matrix")) {
                    critPacket(1.0E-6, false);
                    critPacket(0., false);
                }
                if (mode.is("FunTime")) {
                    if (mc.player.isOnGround()) critPacket(1e-8, false);
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-9, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-9, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                }
            }
        }

        private void critPacket(double yDelta, boolean full) {
            if (full)
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), false));
            else
                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY() + yDelta, mc.player.getPosZ(), ((ClientPlayerEntity) mc.player).lastReportedYaw, ((ClientPlayerEntity) mc.player).lastReportedPitch, false));
        }

    }
