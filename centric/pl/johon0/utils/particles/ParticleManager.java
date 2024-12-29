package centric.pl.johon0.utils.particles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleManager {
    private final List<Particle> particles = new ArrayList();

    public ParticleManager() {
    }

    public void addParticle(Particle particle) {
        this.particles.add(particle);
    }

    public void update() {
        this.particles.removeIf((p) -> {
            return !p.isAlive();
        });
        Iterator var1 = this.particles.iterator();

        while(var1.hasNext()) {
            Particle particle = (Particle)var1.next();
            particle.update();
        }

    }

    public void render() {
        Iterator var1 = this.particles.iterator();

        while(var1.hasNext()) {
            Particle particle = (Particle)var1.next();
            particle.render();
        }

    }
}
