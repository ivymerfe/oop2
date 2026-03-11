package labs.labgame.tui;

import com.badlogic.gdx.math.Vector2;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import labs.labgame.model.Block;
import labs.labgame.model.Bullet;
import labs.labgame.model.Effect;
import labs.labgame.model.Enemy;
import labs.labgame.model.Entity;
import labs.labgame.model.ExplosionEffect;
import labs.labgame.model.GameModel;
import labs.labgame.model.Player;

public class TuiRenderer {
    private static final int HUD_ROWS = 3;
    private static final float CELLS_X = 4.0f;
    private static final float CELLS_Y = 2.0f;
    private static final TextColor BACKGROUND = TextColor.ANSI.BLACK;
    private static final TextColor HUD_BACKGROUND = TextColor.ANSI.BLACK;
    private static final TextColor HUD_ACCENT = TextColor.ANSI.RED_BRIGHT;
    private static final TextColor SKY_HIGH = TextColor.ANSI.WHITE;
    private static final TextColor SKY_MID = TextColor.ANSI.CYAN;
    private static final TextColor SKY_LOW = TextColor.ANSI.BLUE;
    private static final TextColor SOIL = TextColor.ANSI.GREEN;

    private final GameModel model;

    public TuiRenderer(GameModel model) {
        this.model = model;
    }

    public Viewport createViewport(TerminalSize size) {
        int gameRows = Math.max(1, size.getRows() - HUD_ROWS);
        Player player = model.getPlayer();

        float cameraX = player.getX();
        float cameraY = Math.max(7.0f, player.getY() + 3.0f);
        float worldWidth = size.getColumns() / CELLS_X;
        float worldHeight = gameRows / CELLS_Y;
        float viewportLeft = cameraX - worldWidth / 2.0f;
        float viewportBottom = cameraY - worldHeight / 2.0f;
        float viewportTop = viewportBottom + worldHeight;

        return new Viewport(viewportLeft, viewportBottom, viewportTop, viewportLeft + worldWidth, gameRows, size.getColumns());
    }

    public void render(Screen screen) {
        screen.clear();

        TerminalSize size = screen.getTerminalSize();
        Viewport viewport = createViewport(size);
        Player player = model.getPlayer();
        int enemies = 0;
        drawBackground(screen, viewport);
        drawGround(screen, viewport);

        for (Entity entity : model.getEntities().values()) {
            if (entity instanceof Block block) {
                drawBlock(screen, block, viewport);
            } else if (entity instanceof Enemy enemy) {
                drawEnemy(screen, enemy, viewport);
                enemies += 1;
            } else if (entity instanceof Bullet bullet) {
                drawBullet(screen, bullet, viewport);
            }
        }

        drawPlayer(screen, player, viewport);

        for (Effect effect : model.getEffects()) {
            if (effect instanceof ExplosionEffect explosion) {
                float radius = explosion.getRadius() * (0.4f + 0.6f * effect.getProgress());
                drawCircle(screen, effect.getPosition().x, effect.getPosition().y, radius, '.', TextColor.ANSI.YELLOW_BRIGHT, viewport);
            }
        }
        drawHud(screen.newTextGraphics(), size, player, enemies);
    }

    public record Viewport(float left, float bottom, float top, float right, int gameRows, int columns) {
        public boolean contains(int column, int row) {
            return column >= 0 && column < columns && row >= HUD_ROWS && row < HUD_ROWS + gameRows;
        }

        public Vector2 cellCenterToWorld(int column, int row) {
            float worldX = left + (column + 0.5f) / CELLS_X;
            float worldY = bottom + (gameRows - (row - HUD_ROWS) - 0.5f) / CELLS_Y;
            return new Vector2(worldX, worldY);
        }
    }

    private void drawHud(TextGraphics graphics, TerminalSize size, Player player, int enemies) {
        float ratio = player.getHealth() / 100.0f;
        int filledCells = Math.max(0, Math.min(18, Math.round(ratio * 18)));
        String healthBar = String.valueOf('=').repeat(filledCells) + String.valueOf('-').repeat(18 - filledCells);
        String hudTop = String.format("Time %5.1f Enemies %d Best %d Score %d HP [%s] %3.0f  ", model.getTime(), enemies, player.maxScore, player.score, healthBar, player.getHealth());
        String separator = "-".repeat(size.getColumns());

        drawBanner(graphics, size, size.getRows() - 2, separator, TextColor.ANSI.RED, BACKGROUND);
        drawBanner(graphics, size, size.getRows() - 1, hudTop, HUD_ACCENT, HUD_BACKGROUND);
    }

    private void drawBackground(Screen screen, Viewport viewport) {
        int columns = viewport.columns();
        int gameRowEnd = HUD_ROWS + viewport.gameRows();
        int leftCell = (int) Math.floor(viewport.left() * CELLS_X);

        for (int row = HUD_ROWS; row < gameRowEnd; row++) {
            float worldY = viewport.bottom() + (viewport.gameRows() - (row - HUD_ROWS) - 0.5f) / CELLS_Y;
            float worldX = viewport.left() + 0.5f / CELLS_X;
            int noiseY = (int) Math.floor(worldY * 2.0f);
            TextColor skyColor = worldY > 18.0f ? SKY_HIGH : worldY > 8.0f ? SKY_MID : SKY_LOW;

            for (int column = 0; column < columns; column++) {
                char symbol;
                TextColor color;
                if (worldY < 1.0f) {
                    int pattern = Math.floorMod(hashCell(column + leftCell, row), 5);
                    symbol = pattern <= 1 ? '.' : ':';
                    color = SOIL;
                } else {
                    int noise = Math.floorMod(hashCell((int) Math.floor(worldX * 3.0f), noiseY), 97);
                    if (noise == 0) {
                        symbol = '.';
                    } else if (noise == 1 && worldY > 6.0f) {
                        symbol = ',';
                    } else {
                        symbol = ' ';
                    }
                    color = skyColor;
                }
                setCell(screen, column, row, symbol, color);
                worldX += 1.0f / CELLS_X;
            }
        }
    }

    private void drawGround(Screen screen, Viewport viewport) {
        int row = HUD_ROWS + (int) Math.floor((viewport.top() - 1.0f) * CELLS_Y);
        if (row < HUD_ROWS || row >= HUD_ROWS + viewport.gameRows()) {
            return;
        }

        for (int column = 0; column < viewport.columns(); column++) {
            setCell(screen, column, row, '-', TextColor.ANSI.GREEN_BRIGHT);
        }

        int tickSpacing = Math.max(2, Math.round(CELLS_X * 2.0f));
        int startColumn = tickSpacing - Math.floorMod((int) Math.floor(viewport.left() * CELLS_X), tickSpacing);
        for (int column = startColumn; column < viewport.columns(); column += tickSpacing) {
            setCell(screen, column, row, '+', TextColor.ANSI.GREEN_BRIGHT);
        }
    }

    private void drawPlayer(Screen screen, Player player, Viewport viewport) {
        drawRect(screen, player.getX(), player.getY(), player.getWidth(), player.getHeight(), '%', TextColor.ANSI.WHITE_BRIGHT, viewport);
    }

    private void drawEnemy(Screen screen, Enemy enemy, Viewport viewport) {
        drawRect(screen, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), 'H', TextColor.ANSI.MAGENTA, viewport);
    }

    private void drawBlock(Screen screen, Block block, Viewport viewport) {
        drawRect(screen, block.getX(), block.getY(), block.getSize(), block.getSize(), '#', TextColor.ANSI.RED, viewport);
    }

    private void drawBullet(Screen screen, Bullet bullet, Viewport viewport) {
        drawCircle(screen, bullet.getX(), bullet.getY(), Bullet.RADIUS, 'o', TextColor.ANSI.YELLOW, viewport);
    }

    private void drawBanner(TextGraphics graphics, TerminalSize size, int row, String value, TextColor color, TextColor background) {
        graphics.setForegroundColor(color);
        graphics.setBackgroundColor(background);
        graphics.putString(0, row, " ".repeat(size.getColumns()));
        drawString(graphics, 0, row, value, size.getColumns(), color);
    }

    private void drawString(TextGraphics graphics, int column, int row, String value, int maxWidth, TextColor color) {
        if (column >= maxWidth) {
            return;
        }
        graphics.setForegroundColor(color);
        String clipped = value.length() <= maxWidth - column ? value : value.substring(0, Math.max(0, maxWidth - column));
        graphics.putString(column, row, clipped);
    }

    private void drawRect(Screen screen, float centerX, float centerY, float width, float height, char symbol, TextColor color, Viewport viewport) {
        centerX = (float) (Math.floor(centerX * 100) / 100);
        centerY = (float) (Math.floor(centerY * 100) / 100);

        int startColumn = clampColumn(centerX - width / 2.0f, viewport);
        int endColumn = clampColumnEnd(centerX + width / 2.0f, viewport);
        int startRow = clampTopRow(centerY + height / 2.0f, viewport);
        int endRow = clampBottomRow(centerY - height / 2.0f, viewport);

        for (int row = startRow; row <= endRow; row++) {
            for (int column = startColumn; column <= endColumn; column++) {
                setCell(screen, column, row, symbol, color);
            }
        }
    }

    private void drawCircle(Screen screen, float centerX, float centerY, float radius, char symbol, TextColor color, Viewport viewport) {
        int startColumn = clampColumn(centerX - radius, viewport);
        int endColumn = clampColumnEnd(centerX + radius, viewport);
        int startRow = clampTopRow(centerY + radius, viewport);
        int endRow = clampBottomRow(centerY - radius, viewport);
        float radiusSquared = radius * radius;

        for (int row = startRow; row <= endRow; row++) {
            float worldY = rowToWorldY(row, viewport);
            float deltaY = worldY - centerY;
            float worldX = columnToWorldX(startColumn, viewport);
            for (int column = startColumn; column <= endColumn; column++) {
                float deltaX = worldX - centerX;
                if (deltaX * deltaX + deltaY * deltaY <= radiusSquared) {
                    setCell(screen, column, row, symbol, color);
                }
                worldX += 1.0f / CELLS_X;
            }
        }
    }

    private int clampColumn(float worldX, Viewport viewport) {
        int column = (int) Math.floor((worldX - viewport.left()) * CELLS_X);
        return Math.max(0, column);
    }

    private int clampColumnEnd(float worldX, Viewport viewport) {
        int column = (int) Math.floor((worldX - viewport.left()) * CELLS_X);
        return Math.min(viewport.columns() - 1, column);
    }

    private int clampTopRow(float worldY, Viewport viewport) {
        int row = HUD_ROWS + (int) Math.floor((viewport.top() - worldY) * CELLS_Y);
        return Math.max(HUD_ROWS, row);
    }

    private int clampBottomRow(float worldY, Viewport viewport) {
        int row = HUD_ROWS + (int) Math.floor((viewport.top() - worldY) * CELLS_Y);
        return Math.min(HUD_ROWS + viewport.gameRows() - 1, row);
    }

    private float columnToWorldX(int column, Viewport viewport) {
        return viewport.left() + (column + 0.5f) / CELLS_X;
    }

    private float rowToWorldY(int row, Viewport viewport) {
        return viewport.bottom() + (viewport.gameRows() - (row - HUD_ROWS) - 0.5f) / CELLS_Y;
    }

    private void setCell(Screen screen, int column, int row, char symbol, TextColor color) {
        screen.setCharacter(column, row, new TextCharacter(symbol, color, BACKGROUND));
    }

    private int hashCell(int x, int y) {
        int hash = x * 73428767 ^ y * 912931;
        hash ^= hash >>> 13;
        return hash;
    }
}
