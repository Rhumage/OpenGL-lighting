package vaja2;

/**
 *
 *  Q  -  move camera target up
 *  E  -  move camera target down
 *  W  -  move camera target forward
 *  S  -  move camera target backwards
 *  A  -  move camera target left
 *  D  -  move camera target right
 *  Space  -  reset camera target and rotation, reset light position
 *  LMB  -  grab mouse
 *  Mouse  -  rotate camera
 *  U  -  move light up
 *  O  -  move light down
 *  I  -  move light forward
 *  K  -  move light backwards
 *  J  -  move light left
 *  L  -  move light right
 *  Escape - release mouse / exit the program
 *  +, R  -  add ball
 *  -, F  -  remove ball
 *  T, Tab  -  switch to next ball
 *  G  -  switch to previous ball
 *  F11  -  switch between fullscreen and windowed mode
 *  Y  -  switch between wood and marble
 *  X  -  switch between Gouraud shading and Phong model
 *  C  -  show / hide light
 *  V  -  enable / disable textures
 *  Mouse scrool  -  zoom
*/
import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Vaja2 {
    public static final String PATH = "D:\\MyFiles\\lwjgl\\RG\\Vaja2\\build\\classes\\vaja2\\";
    private static final String PHONG_VERT_LOCATION = "phong.vert";
    private static final String PHONG_FRAG_LOCATION = "phong.frag";
    private static final String GOURAUD_VERT_LOCATION = "gouraud.vert";
    private static final String GOURAUD_FRAG_LOCATION = "gouraud.frag";
    private static final String PHONG_NO_TEX_VERT_LOCATION = "phongNoTex.vert";
    private static final String PHONG_NO_TEX_FRAG_LOCATION = "phongNoTex.frag";
    private static final String GOURAUD_NO_TEX_VERT_LOCATION = "gouraudNoTex.vert";
    private static final String GOURAUD_NO_TEX_FRAG_LOCATION = "gouraudNoTex.frag";
    private static final String LIGHT_VERT_LOCATION = "light.vert";
    private static final String LIGHT_FRAG_LOCATION = "light.frag";
    private static final String WOOD_LOCATION = "wood.png";
    private static final String MARBLE_LOCATION = "marble.png";
    
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int FS_WIDTH = 1920;
    private static final int FS_HEIGHT = 1080;
    
    private Vector4f lightIntensity = new Vector4f(0.8f, 0.8f, 0.8f, 1.0f);
    private Vector4f ambientIntensity = new Vector4f(0.4f, 0.4f, 0.4f, 1.0f);
    private Vector4f diffuseColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Vector4f redDiffuseColor = new Vector4f(1.0f, 0.5f, 0.5f, 1.0f);
    private float lightAttenuation = 0.01f;
    private float shininessFactor = 4.0f;
    private static boolean continueMainLoop;
    private boolean drawLight = true;
    
    private Ball currentBall;
    private ArrayList<Ball> balls = new ArrayList<>();
    private final float minBallSize = 0.1f, maxBallSize = 1.0f, maxNumBalls = 20, ballArea = 50.0f, planeSize = 4;
    private Plane plane;
    private Light light;
        
    private ProgramData phongProgram;
    private ProgramData phongNTProgram;
    private ProgramData gouraudProgram;
    private ProgramData gouraudNTProgram;
    private ProgramData lightProgram;
    private boolean gouraud;
    private boolean textures = true;
    
    private float fovY = 45.0f;
    private float zNear = 0.01f;
    private float zFar = 100.0f;
    
    private float movementSpeed = 4.0f;
    private float mouseSpeed = 0.01f;
    private float zoomSpeed = 0.2f, minZoom = 1.0f, maxZoom = 150.0f;
    
    private float elapsedTime;
    private float lastFrameDuration;
    private double lastFrameTimeStamp, now;
    
    private Vector3f camTarget = new Vector3f(0.0f, 5.0f, 20.0f);
    private Vector3f sphereCamRelPos = new Vector3f(90.0f, 0.0f, 0.0f);
    
    private int[] texIds = new int[] {0, 0};
    private boolean textureType = true;
    
    private int globalMatricesUBO;
    private final int globalMatricesBindingIndex = 0;
    
    private void start() {
        try {
            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle("Vaja 2");
            Display.setResizable(true);
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        init();
        long startTime = System.nanoTime();
        
        currentBall = createBall();
        plane = new Plane(0.0f, -5.0f, -22.0f, planeSize);
        light = new Light(new Vector3f(-10.0f, 30.0f, 0.0f), 0.1f);
                
        continueMainLoop = true;
        gouraud = false;
        reshape(WIDTH, HEIGHT);
        
        while (continueMainLoop && !Display.isCloseRequested()) {
            
            elapsedTime = (float) ((System.nanoTime() - startTime) / 1000000.0);
            now = System.nanoTime();
            lastFrameDuration = (float) ((now - lastFrameTimeStamp) / 1000000.0);
            lastFrameTimeStamp = now;
            
            update();
            display();
            
            Display.update();
            Display.sync(60);
            
            if (Display.wasResized()) {
                reshape(Display.getWidth(), Display.getHeight());
            }
        }
        
        Display.destroy();
    }
    
    private void init() {
        initializeProgram();
        
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CW);
        
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glDepthMask(true);
        glDepthRange(0.0, 1.0);
        glEnable(GL_DEPTH_CLAMP);
        
        setupTextures();
        glActiveTexture(GL_TEXTURE0);
    }
    
    private void setupTextures() {
        texIds[0] = this.loadPNGTexture(WOOD_LOCATION, GL_TEXTURE0);
        texIds[1] = this.loadPNGTexture(MARBLE_LOCATION, GL_TEXTURE0);
    }
    
    private int loadPNGTexture(String filename, int textureUnit) {
        ByteBuffer buf = null;
        int tWidth = 0;
        int tHeight = 0;

        try {
            InputStream in = new FileInputStream(PATH + filename);
            PNGDecoder decoder = new PNGDecoder(in);

            tWidth = decoder.getWidth();
            tHeight = decoder.getHeight();

            buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        int texId = glGenTextures();
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D, texId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, tWidth, tHeight, 0,
        GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER,
        GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,
        GL_LINEAR_MIPMAP_LINEAR);

        return texId;
    }
    
    private void initializeProgram() {
        phongProgram = loadPhongProgram(PHONG_VERT_LOCATION, PHONG_FRAG_LOCATION);
        phongNTProgram = loadPhongProgram(PHONG_NO_TEX_VERT_LOCATION, PHONG_NO_TEX_FRAG_LOCATION);
        gouraudProgram = loadGouraudProgram(GOURAUD_VERT_LOCATION, GOURAUD_FRAG_LOCATION);
        gouraudNTProgram = loadGouraudProgram(GOURAUD_NO_TEX_VERT_LOCATION, GOURAUD_NO_TEX_FRAG_LOCATION);
        lightProgram = loadLightProgram(LIGHT_VERT_LOCATION, LIGHT_FRAG_LOCATION);
        
        globalMatricesUBO = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, globalMatricesUBO);
        glBufferData(GL_UNIFORM_BUFFER, Float.SIZE / Byte.SIZE * 16, GL_STREAM_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        glBindBufferRange(GL_UNIFORM_BUFFER, globalMatricesBindingIndex, globalMatricesUBO, 0, 16);
    }
    
    private ProgramData loadGouraudProgram(String vertLocation, String fragLocation) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(loadShader(GL_VERTEX_SHADER, vertLocation));
        shaderList.add(loadShader(GL_FRAGMENT_SHADER, fragLocation));
        
        ProgramData data = new ProgramData();
        data.program = createProgram(shaderList);
        
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.program, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.program, "normalModelToCameraMatrix");
        data.globalUniformBlockIndex = glGetUniformBlockIndex(data.program, "GlobalMatrices");
        data.dirToLight = glGetUniformLocation(data.program, "dirToLight");
        data.lightIntensityUnif = glGetUniformLocation(data.program, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.program, "ambientIntensity");
        data.baseDiffuseColorUnif = glGetUniformLocation(data.program, "baseDiffuseColor");
        data.cameraToClipMatrixUnif = glGetUniformLocation(data.program, "cameraToClipMatrix2");
        
        for (Integer shader : shaderList) {
            glDeleteShader(shader);
        }
        return data;
    }
    
    private ProgramData loadPhongProgram(String vertLocation, String fragLocation) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(loadShader(GL_VERTEX_SHADER, vertLocation));
        shaderList.add(loadShader(GL_FRAGMENT_SHADER, fragLocation));
        
        ProgramData data = new ProgramData();
        data.program = createProgram(shaderList);
        
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.program, "modelToCameraMatrix");
        data.normalModelToCameraMatrixUnif = glGetUniformLocation(data.program, "normalModelToCameraMatrix");
        data.globalUniformBlockIndex = glGetUniformBlockIndex(data.program, "GlobalMatrices");
        data.cameraSpaceLightPosUnif = glGetUniformLocation(data.program, "cameraSpaceLightPos");
        data.lightIntensityUnif = glGetUniformLocation(data.program, "lightIntensity");
        data.ambientIntensityUnif = glGetUniformLocation(data.program, "ambientIntensity");
        data.lightAttenuationUnif = glGetUniformLocation(data.program, "lightAttenuation");
        data.shininessFactorUnif = glGetUniformLocation(data.program, "shininessFactor");
        data.baseDiffuseColorUnif = glGetUniformLocation(data.program, "baseDiffuseColor");
        data.cameraToClipMatrixUnif = glGetUniformLocation(data.program, "cameraToClipMatrix2");
        
        for (Integer shader : shaderList) {
            glDeleteShader(shader);
        }
        return data;
    }
    
    private ProgramData loadLightProgram(String vertLocation, String fragLocation) {
        ArrayList<Integer> shaderList = new ArrayList<>();
        shaderList.add(loadShader(GL_VERTEX_SHADER, vertLocation));
        shaderList.add(loadShader(GL_FRAGMENT_SHADER, fragLocation));
        
        ProgramData data = new ProgramData();
        data.program = createProgram(shaderList);
        
        data.posOffset = glGetUniformLocation(data.program, "posOffset");
        data.modelToCameraMatrixUnif = glGetUniformLocation(data.program, "modelToCameraMatrix");
        data.globalUniformBlockIndex = glGetUniformBlockIndex(data.program, "GlobalMatrices");
        
        for (Integer shader : shaderList) {
            glDeleteShader(shader);
        }
        return data;
    }
    
    private int createProgram(ArrayList<Integer> shaderList) {
        int program = glCreateProgram();
        for (Integer shader : shaderList) {
            glAttachShader(program, shader);
        }
        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            System.err.println("Failed to link program");
            System.exit(1);
        }
        for (Integer shader : shaderList) {
            glDetachShader(program, shader);
        }
        return program;
    }
    
    private int loadShader(int type, String location) {
        int shader = glCreateShader(type);
        String source = readShader(location);
        glShaderSource(shader, source);
        glCompileShader(shader);
        
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            System.err.println("Failed to compile shader " + location);
            System.exit(1);
        }
        return shader;
    }
    
    private String readShader(String location) {
        StringBuilder source = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(PATH + location))) {
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source.toString();
    }
    
    private void update() {
        float delta = getLastFrameDuration() * 10 / 1000.0f;
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_ESCAPE:
                        if (Mouse.isGrabbed()) {
                            Mouse.setGrabbed(false);
                        } else {
                            leaveMainLoop();
                        }
                        break;
                    case Keyboard.KEY_SPACE:
                        camTarget = new Vector3f(0.0f, 5.0f, 20.0f);
                        sphereCamRelPos = new Vector3f(90.0f, 0.0f, 0.0f);
                        light.setPosition(new Vector3f(-10.0f, 30.0f, 0.0f));
                        break;
                    case Keyboard.KEY_R:
                        addBall();
                        break;
                    case Keyboard.KEY_F:
                        removeBall();
                        break;
                    case Keyboard.KEY_T:
                        nextBall();
                        break;
                    case Keyboard.KEY_G:
                        previousBall();
                        break;
                    case Keyboard.KEY_ADD:
                        addBall();
                        break;
                    case Keyboard.KEY_SUBTRACT:
                        removeBall();
                        break;
                    case Keyboard.KEY_TAB:
                        nextBall();
                        break;
                    case Keyboard.KEY_F11:
                            if (Display.isFullscreen()) {
                                setDisplayMode(WIDTH, HEIGHT, false);
                            } else {
                                setDisplayMode(FS_WIDTH, FS_HEIGHT, true);
                            }
                            reshape(Display.getWidth(), Display.getHeight());
                        break;
                    case Keyboard.KEY_X:
                        gouraud = !gouraud;
                        break;
                    case Keyboard.KEY_C:
                        drawLight = !drawLight;
                        break;
                    case Keyboard.KEY_Y:
                        textureType = !textureType;
                        break;
                    case Keyboard.KEY_V:
                        textures = !textures;
                        break;
                }
            }
        }
        
        boolean keyW = Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyS = Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyA = Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyD = Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean keyQ = Keyboard.isKeyDown(Keyboard.KEY_Q);
        boolean keyE = Keyboard.isKeyDown(Keyboard.KEY_E);
        
        if (keyW && !keyS && keyA && !keyD) {
            float angle = sphereCamRelPos.x + 45;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (keyW && !keyS && !keyA && keyD) {
            float angle = sphereCamRelPos.x + 135;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (!keyW && keyS && keyA && !keyD) {
            float angle = sphereCamRelPos.x - 45;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (!keyW && keyS && !keyA && keyD) {
            float angle = sphereCamRelPos.x - 135;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (keyW && !keyS && !keyA && !keyD) {
            float angle = sphereCamRelPos.x + 90;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (!keyW && keyS && !keyA && !keyD) {
            float angle = sphereCamRelPos.x - 90;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (!keyW && !keyS && keyA && !keyD) {
            float angle = sphereCamRelPos.x;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        } else if (!keyW && !keyS && !keyA && keyD) {
            float angle = sphereCamRelPos.x + 180;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            camTarget.z += adjacent;
            camTarget.x -= opposite;
        }
        if (keyQ && !keyE) {
            camTarget.y += movementSpeed * delta;
        } else if (!keyQ && keyE) {
            camTarget.y -= movementSpeed * delta;
        }
        
        boolean keyI = Keyboard.isKeyDown(Keyboard.KEY_I);
        boolean keyK = Keyboard.isKeyDown(Keyboard.KEY_K);
        boolean keyJ = Keyboard.isKeyDown(Keyboard.KEY_J);
        boolean keyL = Keyboard.isKeyDown(Keyboard.KEY_L);
        boolean keyU = Keyboard.isKeyDown(Keyboard.KEY_U);
        boolean keyO = Keyboard.isKeyDown(Keyboard.KEY_O);
        
        if (keyI && !keyK && keyJ && !keyL) {
            float angle = sphereCamRelPos.x + 45;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (keyI && !keyK && !keyJ && keyL) {
            float angle = sphereCamRelPos.x + 135;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (!keyI && keyK && keyJ && !keyL) {
            float angle = sphereCamRelPos.x - 45;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (!keyI && keyK && !keyJ && keyL) {
            float angle = sphereCamRelPos.x - 135;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (keyI && !keyK && !keyJ && !keyL) {
            float angle = sphereCamRelPos.x + 90;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (!keyI && keyK && !keyJ && !keyL) {
            float angle = sphereCamRelPos.x - 90;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (!keyI && !keyK && keyJ && !keyL) {
            float angle = sphereCamRelPos.x;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        } else if (!keyI && !keyK && !keyJ && keyL) {
            float angle = sphereCamRelPos.x + 180;
            float hypotenuse = movementSpeed * delta;
            float adjacent = hypotenuse * (float) Math.cos(Math.toRadians(angle));
            float opposite = (float) (Math.sin(Math.toRadians(angle)) * hypotenuse);
            light.setZ(light.getZ() + adjacent);
            light.setX(light.getX() - opposite);
        }
        if (keyU && !keyO) {
            light.setY(light.getY() + movementSpeed * delta);
        } else if (!keyU && keyO) {
            light.setY(light.getY() - movementSpeed * delta);
        }
        
        while (Mouse.next()) {
            if (Mouse.getEventButton() == 0) {
                Mouse.setGrabbed(true);
            }
        }
        if (Mouse.isGrabbed()) {
            sphereCamRelPos.y = sphereCamRelPos.y + Mouse.getDY() * mouseSpeed * lastFrameDuration;
            sphereCamRelPos.x = sphereCamRelPos.x + Mouse.getDX() * mouseSpeed * lastFrameDuration;
        }
        float zoom = Mouse.getDWheel();
        if (zoom != 0) {
            sphereCamRelPos.z -= zoom * zoomSpeed * delta;
        }
        
        sphereCamRelPos.y = Math.min(Math.max(sphereCamRelPos.y, -78.75f), 78.5f);
        camTarget.y = camTarget.y > 0.0f ? camTarget.y : 0.0f;
        sphereCamRelPos.z = Math.min(Math.max(sphereCamRelPos.z, minZoom), maxZoom);//sphereCamRelPos.z > 1.0f ? sphereCamRelPos.z : 1.0f;
    }

    private Vector3f resolveCamPosition() {
        float phi = (float) Math.toRadians(sphereCamRelPos.x);
        float theta = (float) Math.toRadians(sphereCamRelPos.y + 90.0f);
        Vector3f dirToCamera = new Vector3f((float) (Math.sin(theta) * (float) Math.cos(phi)), (float) Math.cos(theta), (float) (Math.sin(theta) * Math.sin(phi)));
        return Vector3f.add((Vector3f) dirToCamera.scale(sphereCamRelPos.z), camTarget, null);
    }
    
    private Matrix4f calcLookAtMatrix(Vector3f cameraPt, Vector3f lookPt, Vector3f upPt) {
        Vector3f lookDir = (Vector3f) (Vector3f.sub(lookPt, cameraPt, null)).normalise();
        Vector3f upDir = (Vector3f) upPt.normalise();
        Vector3f rightDir = (Vector3f) (Vector3f.cross(lookDir, upDir, null)).normalise();
        Vector3f perpUpDir = Vector3f.cross(rightDir, lookDir, null);

        Matrix4f rotMat = new Matrix4f();
        rotMat.m00 = rightDir.x;
        rotMat.m01 = rightDir.y;
        rotMat.m02 = rightDir.z;
        rotMat.m10 = perpUpDir.x;
        rotMat.m11 = perpUpDir.y;
        rotMat.m12 = perpUpDir.z;
        rotMat.m20 = -lookDir.x;
        rotMat.m21 = -lookDir.y;
        rotMat.m22 = -lookDir.z;
        rotMat.transpose();

        Matrix4f transMat = new Matrix4f();
        transMat.m30 = -cameraPt.x;
        transMat.m31 = -cameraPt.y;
        transMat.m32 = -cameraPt.z;

        return Matrix4f.mul(rotMat, transMat, null);
    }
    
    private Ball createBall() {
        Random r = new Random();
        float ballSize = r.nextFloat() * (maxBallSize - minBallSize) + minBallSize;
        float ballX = r.nextFloat() * ballArea - ballArea / 2;
        float ballZ = -r.nextFloat() * ballArea;
        while (intersects(ballSize, ballX, ballZ)) {
            ballSize = r.nextFloat() * (maxBallSize - minBallSize) + minBallSize;
            ballX = r.nextFloat() * ballArea - ballArea / 2;
            ballZ = -r.nextFloat() * ballArea + 5;
        }
        return new Ball(ballX, -5, ballZ, ballSize);
    }
    
    private boolean intersects(float size, float X, float Z) {
        for (Ball ball : balls) {
            boolean intersectsX = X > ball.getPosition().x - ball.getSize() * 12 - size * 12 && X < ball.getPosition().x + ball.getSize() * 12 + size * 12;
            boolean intersectsZ = Z > ball.getPosition().z - ball.getSize() * 12 - size * 12 && Z < ball.getPosition().z + ball.getSize() * 12 + size * 12;
            if (intersectsX && intersectsZ)
                return true;
        }
        return false;
    }
    
    private void addBall() {
        if (balls.size() < maxNumBalls) {
            balls.add(new Ball(currentBall));
            currentBall = createBall();
        }
    }
    
    private void removeBall() {
        if (balls.size() > 0)
            currentBall = balls.remove(balls.size() - 1);
    }
    
    private void nextBall() {
        balls.add(currentBall);
        currentBall = balls.remove(0);
    }
    
    private void previousBall() {
        balls.add(0, currentBall);
        removeBall();
    }
    
    private void display() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Matrix4f modelView;
        
        final Vector3f camPos = resolveCamPosition();
        modelView = calcLookAtMatrix(camPos, camTarget, new Vector3f(0.0f, 1.0f, 0.0f));
        
        if (drawLight) {
            glUseProgram(lightProgram.program);
            glUniform3(lightProgram.posOffset, fillAndFlipBuffer(light.getPosition()));
            glUniformMatrix4(lightProgram.modelToCameraMatrixUnif, false, fillAndFlipBuffer(modelView));
            light.draw();
            glUseProgram(0);
        }
        
        ProgramData current = gouraud ? textures ? gouraudProgram : gouraudNTProgram : textures ? phongProgram : phongNTProgram;
        
        Matrix3f normMatrix = new Matrix3f();
        normMatrix.load(mat4to3(modelView));
        if (gouraud) {
            glUseProgram(current.program);
            glUniform4f(current.lightIntensityUnif, lightIntensity.x, lightIntensity.y, lightIntensity.z, lightIntensity.w);
            glUniform4f(current.ambientIntensityUnif, ambientIntensity.x, ambientIntensity.y, ambientIntensity.z, ambientIntensity.w);
            glUniform4(current.baseDiffuseColorUnif, fillAndFlipBuffer(diffuseColor));
            glUniformMatrix4(current.modelToCameraMatrixUnif, false, fillAndFlipBuffer(modelView));
            glUniformMatrix3(current.normalModelToCameraMatrixUnif, false, fillAndFlipBuffer(normMatrix));
            glUniformMatrix4(current.cameraToClipMatrixUnif, false, fillAndFlipBuffer(reshape(WIDTH, HEIGHT)));
        } else {
            final Vector4f lightPosCameraSpace = mul(modelView, new Vector4f(light.getX(), light.getY(), light.getZ(), 1.0f));
            glUseProgram(current.program);
            glUniform3(current.cameraSpaceLightPosUnif, fillAndFlipBuffer(lightPosCameraSpace));
            glUniform4f(current.lightIntensityUnif, lightIntensity.x, lightIntensity.y, lightIntensity.z, lightIntensity.w);
            glUniform4f(current.ambientIntensityUnif, ambientIntensity.x, ambientIntensity.y, ambientIntensity.z, ambientIntensity.w);
            glUniform1f(current.lightAttenuationUnif, lightAttenuation);
            glUniform1f(current.shininessFactorUnif, shininessFactor);
            glUniform4(current.baseDiffuseColorUnif, fillAndFlipBuffer(diffuseColor));
            glUniformMatrix4(current.modelToCameraMatrixUnif, false, fillAndFlipBuffer(modelView));
            glUniformMatrix3(current.normalModelToCameraMatrixUnif, false, fillAndFlipBuffer(normMatrix));
            glUniformMatrix4(current.cameraToClipMatrixUnif, false, fillAndFlipBuffer(reshape(WIDTH, HEIGHT)));
        }
        
        if (gouraud) {
            Vector4f pos = new Vector4f(0, -5, -15, 1.0f);
            Vector4f lightDirection = Vector4f.sub(new Vector4f(light.getX(), light.getY(), light.getZ(), 1.0f) , pos, null);
            final Vector4f lightDirCameraSpace = mul(modelView, lightDirection);
            glUniform3(current.dirToLight, fillAndFlipBuffer(lightDirCameraSpace));
        }
        
        glBindTexture(GL_TEXTURE_2D, texIds[textureType ? 0 : 1]);
        glUniformMatrix4(current.modelToCameraMatrixUnif, false, fillAndFlipBuffer(new Matrix4f(modelView).translate(plane.getPosition())));
        normMatrix = new Matrix3f();
        normMatrix.load(mat4to3(modelView));
        glUniformMatrix3(current.normalModelToCameraMatrixUnif, false, fillAndFlipBuffer(normMatrix));
        plane.draw();
        
        glBindTexture(GL_TEXTURE_2D, texIds[textureType ? 1 : 0]);
        for (Ball ball : balls) {
            if (gouraud) {
                Vector4f pos = new Vector4f(ball.getPosition().x, ball.getPosition().y, ball.getPosition().z, 1.0f);
                Vector4f lightDirection = Vector4f.sub(new Vector4f(light.getX(), light.getY(), light.getZ(), 1.0f) , pos, null);
                final Vector4f lightDirCameraSpace = mul(modelView, lightDirection);
                glUniform3(current.dirToLight, fillAndFlipBuffer(lightDirCameraSpace));
            }
            glUniformMatrix4(current.modelToCameraMatrixUnif, false, fillAndFlipBuffer(new Matrix4f(modelView).translate(ball.getPosition())));
            normMatrix = new Matrix3f();
            normMatrix.load(mat4to3(modelView));
            glUniformMatrix3(current.normalModelToCameraMatrixUnif, false, fillAndFlipBuffer(normMatrix));
            ball.draw();
        }
        
        glUniform4(current.baseDiffuseColorUnif, fillAndFlipBuffer(redDiffuseColor));
        glUniformMatrix4(current.modelToCameraMatrixUnif, false, fillAndFlipBuffer(new Matrix4f(modelView).translate(currentBall.getPosition())));
        normMatrix = new Matrix3f();
        normMatrix.load(mat4to3(modelView));
        glUniformMatrix3(current.normalModelToCameraMatrixUnif, false, fillAndFlipBuffer(normMatrix));
        if (gouraud) {
            Vector4f pos = new Vector4f(currentBall.getPosition().x, currentBall.getPosition().y, currentBall.getPosition().z, 1.0f);
            Vector4f lightDirection = Vector4f.sub(new Vector4f(light.getX(), light.getY(), light.getZ(), 1.0f) , pos, null);
            final Vector4f lightDirCameraSpace = mul(modelView, lightDirection);
            glUniform3(current.dirToLight, fillAndFlipBuffer(lightDirCameraSpace));
        }
        currentBall.draw();
        glUseProgram(0);   
    }
    
    private Vector4f mul(Matrix4f matrix, Vector4f vec) {
        Vector4f res = new Vector4f();
        res.x = matrix.m00 * vec.x + matrix.m10 * vec.y + matrix.m20 * vec.z + matrix.m30 * vec.w;
        res.y = matrix.m01 * vec.x + matrix.m11 * vec.y + matrix.m21 * vec.z + matrix.m31 * vec.w;
        res.z = matrix.m02 * vec.x + matrix.m12 * vec.y + matrix.m22 * vec.z + matrix.m32 * vec.w;
        res.w = matrix.m03 * vec.x + matrix.m13 * vec.y + matrix.m23 * vec.z + matrix.m33 * vec.w;
        return res;
    }
    
    private Matrix4f reshape(int width, int height) {
        glViewport(0, 0, width, height);
        Matrix4f perspectiveMatrix = new Matrix4f();
        
        float range = (float) (Math.tan(Math.toRadians(fovY / 2.0f)) * zNear);
        float aspect = (width / (float) height);
        float left = -range * aspect;
        float right = range * aspect;
        float bottom = -range;
        float top = range;

        perspectiveMatrix.m00 = (2.0f * zNear) / (right - left);
        perspectiveMatrix.m11 = (2.0f * zNear) / (top - bottom);
        perspectiveMatrix.m22 = -(zFar + zNear) / (zFar - zNear);
        perspectiveMatrix.m23 = -1.0f;
        perspectiveMatrix.m32 = -(2.0f * zFar * zNear) / (zFar - zNear);
        
        glBindBuffer(GL_UNIFORM_BUFFER, globalMatricesUBO);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, fillAndFlipBuffer(perspectiveMatrix));
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        
        return perspectiveMatrix;
    }
    
    private void leaveMainLoop() {
        continueMainLoop = false;
    }
    
    private FloatBuffer fillAndFlipBuffer(Vector3f vector) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        vector.store(buffer);
        buffer.flip();
        return buffer;
    }
    
    private FloatBuffer fillAndFlipBuffer(Vector4f vector) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        vector.store(buffer);
        buffer.flip();
        return buffer;
    }
    
    private FloatBuffer fillAndFlipBuffer(Matrix3f matrix) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        matrix.store(buffer);
        buffer.flip();
        return buffer;
    }
    
    private FloatBuffer fillAndFlipBuffer(Matrix4f matrix) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.store(buffer);
        buffer.flip();
        return buffer;
    }
    
    private Matrix3f mat4to3(Matrix4f mat4) {
        Matrix3f mat3 = new Matrix3f();
        mat3.m00 = mat4.m00;
        mat3.m01 = mat4.m01;
        mat3.m02 = mat4.m02;
        mat3.m10 = mat4.m10;
        mat3.m11 = mat4.m11;
        mat3.m12 = mat4.m12;
        mat3.m20 = mat4.m20;
        mat3.m21 = mat4.m21;
        mat3.m22 = mat4.m22;
        return mat3;
    }
    
    public void setDisplayMode(int width, int height, boolean fullscreen) {
        try {
            DisplayMode targetDisplayMode = null;
            if (fullscreen) {
                DisplayMode[] modes = Display.getAvailableDisplayModes();
                int freq = 0;

                for (int i=0; i < modes.length; i++) {
                    DisplayMode current = modes[i];
                    if (current.getWidth() == width && current.getHeight() == height) {
                        if (targetDisplayMode == null || current.getFrequency() >= freq) {
                            if (targetDisplayMode == null || current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel()) {
                                targetDisplayMode = current;
                                freq = targetDisplayMode.getFrequency();
                            }
                        }
                        if (current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel() && current.getFrequency() == Display.getDesktopDisplayMode().getFrequency()) {
                            targetDisplayMode = current;
                            break;
                        }
                    }
                }
            } else {
                targetDisplayMode = new DisplayMode(width,height);
            }
            if (targetDisplayMode == null) {
                System.out.println("Failed to find value mode: " + width + "x" + height + " fs=" + fullscreen);
                return;
            }
            Display.setDisplayMode(targetDisplayMode);
            Display.setFullscreen(fullscreen);
        } catch (LWJGLException e) {
            System.out.println("Unable to setup mode " + width + "x" + height + " fullscreen=" + fullscreen + e);
        }
    }
    
    private float getLastFrameDuration() {
        return lastFrameDuration;
    }

    public static void main(String[] args) {
        new Vaja2().start();
    }
}
