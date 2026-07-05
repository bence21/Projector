package projector.service;

public class ForkMirrorMigrationResult {

    private int mirrorsMarked;
    private int forksCreated;

    public int getMirrorsMarked() {
        return mirrorsMarked;
    }

    public void incrementMirrorsMarked() {
        ++mirrorsMarked;
    }

    public int getForksCreated() {
        return forksCreated;
    }

    public void incrementForksCreated() {
        ++forksCreated;
    }

    public void incrementServerUpdated() {
    }

    public void incrementSkipped() {
    }

}
