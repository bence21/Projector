package projector.service;

public class ForkMirrorMigrationResult {

    private int mirrorsMarked;
    private int forksCreated;
    private int serverUpdated;
    private int skipped;

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
        ++serverUpdated;
    }

    public void incrementSkipped() {
        ++skipped;
    }
}
