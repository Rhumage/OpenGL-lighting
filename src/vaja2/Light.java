package vaja2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.lwjgl.util.vector.Vector3f;

public class Light {
    private static final String MESH_NAME = "ball.obj";
    
    private Vector3f position = new Vector3f();
    private int vao = 0;
    private int indicesCount;
    
    public Light(float size) {
        BufferedReader reader = null;
        String line;
        ArrayList<float[]> v = new ArrayList<>();
        ArrayList<Short> fV = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(Vaja2.PATH + MESH_NAME));
            while ((line = reader.readLine()) != null) {
                String values[] = line.split(" ");
                switch (values[0]) {
                    case "v":
                        v.add(new float[] {Float.parseFloat(values[2]), Float.parseFloat(values[3]), Float.parseFloat(values[4])});
                        break;
                    case "f":
                        fV.add(Short.parseShort((Integer.parseInt(values[1].split("/")[0]) - 1) + ""));
                        fV.add(Short.parseShort((Integer.parseInt(values[2].split("/")[0]) - 1) + ""));
                        fV.add(Short.parseShort((Integer.parseInt(values[3].split("/")[0]) - 1) + ""));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        float data[] = new float[fV.size() * 3];
        indicesCount = fV.size();
        int k = 0;
        for (int i = 0; i < fV.size(); i++) {
            float vertex[] = v.get(fV.get(i));
            data[k++] = vertex[0];
            data[k++] = vertex[1];
            data[k++] = vertex[2];
        }
        
        FloatBuffer vertexDataBuffer = BufferUtils.createFloatBuffer(data.length);
        vertexDataBuffer.put(data);
        vertexDataBuffer.flip();
        
        int vertexBufferObject = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindVertexArray(0);
    }
    
    public Light(Vector3f position, float size) {
        this(size);
        this.position = position;
    }
    
    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_FAN, 0, indicesCount);
        glBindVertexArray(0);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
    
    public void setX(float x) {
        position.x = x;
    }
    
    public void setY(float y) {
        position.y = y;
    }
    
    public void setZ(float z) {
        position.z = z;
    }
    
    public float getX() {
        return position.x;
    }
    
    public float getY() {
        return position.y;
    }
    
    public float getZ() {
        return position.z;
    }
}
