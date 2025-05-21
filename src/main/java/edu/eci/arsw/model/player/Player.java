    package edu.eci.arsw.model.player;

    import edu.eci.arsw.model.Game;
    import java.util.UUID;

    public abstract class Player {
        private final String id;
        private int x;
        private int y;
        private int speed;
        private final String name;
        private boolean ready;

        public Player(String id, int x, int y, int speed, String name) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.name = name;
            this.ready = false;
        }

        // Getters y setters
        public String getId() { return id; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getSpeed() { return speed; }
        public void setSpeed(int speed) { this.speed = speed; }
        public String getName() { return name; }
        public boolean isReady() { return ready; }
        public void setReady(boolean ready) { this.ready = ready; }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public abstract char getSymbol();
        public abstract boolean canMoveTo(int newX, int newY, Game game);

        protected boolean isValidMove(int newX, int newY, Game game) {
            return game.getMap().isWalkable(newX, newY) &&
                    game.getPlayers().stream()
                            .noneMatch(p -> p != this && p.getX() == newX && p.getY() == newY);
        }

        public void collectPowerUp() {
            if (this instanceof Survivor) {
                ((Survivor) this).enableStamina(10000);
            }
        }

        public void move(int dx, int dy, Game game) {
            int speed = getSpeed();
            int newX = x + (dx * speed);
            int newY = y + (dy * speed);

            if (newX < 0 || newX >= game.getMap().getWidth() ||
                    newY < 0 || newY >= game.getMap().getHeight()) {
                return;
            }

            for (int i = 1; i <= speed; i++) {
                int checkX = x + (dx * i);
                int checkY = y + (dy * i);
                if (game.getMap().getCell(checkX, checkY) == 'P') {
                    game.collectPowerUp(checkX, checkY, this.id);
                }
            }

            if (canMoveTo(newX, newY, game) && game.getMap().isWalkable(newX, newY)) {
                game.getMap().setCell(x, y, '.');

                x = newX;
                y = newY;

                game.getMap().setCell(x, y, getSymbol());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Player player = (Player) o;
            return id.equals(player.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }