package vaja2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Vector3f;

public class Plane {
    private static final String MESH_NAME = "plane.obj";
    
    private Vector3f position = new Vector3f();
    private float size = 0.0f;
    private int vao = 0;
    private int indicesCount;
    
    public Plane(float size) {
        BufferedReader reader;
        String line;
        ArrayList<float[]> v = new ArrayList<>();
        ArrayList<float[]> vn = new ArrayList<>();
        ArrayList<float[]> vt = new ArrayList<>();
        ArrayList<Short> fV = new ArrayList<>();
        ArrayList<Short> fVn = new ArrayList<>();
        ArrayList<Short> fVt = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(Vaja2.PATH + MESH_NAME));
            while ((line = reader.readLine()) != null) {
                String values[] = line.split(" ");
                switch (values[0]) {
                    case "v":
                        v.add(new float[] {Float.parseFloat(values[2]), Float.parseFloat(values[3]), Float.parseFloat(values[4])});
                        break;
                    case "vn":
                        vn.add(new float[] {Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3])});
                        break;
                    case "vt":
                        vt.add(new float[] {Float.parseFloat(values[1]), Float.parseFloat(values[2]), Float.parseFloat(values[3])});
                        break;
                    case "f":
                        fV.add(Short.parseShort((Integer.parseInt(values[1].split("/")[0]) - 1) + ""));
                        fV.add(Short.parseShort((Integer.parseInt(values[2].split("/")[0]) - 1) + ""));
                        fV.add(Short.parseShort((Integer.parseInt(values[3].split("/")[0]) - 1) + ""));
                        fVn.add(Short.parseShort((Integer.parseInt(values[1].split("/")[1]) - 1) + ""));
                        fVn.add(Short.parseShort((Integer.parseInt(values[2].split("/")[1]) - 1) + ""));
                        fVn.add(Short.parseShort((Integer.parseInt(values[3].split("/")[1]) - 1) + ""));
                        fVt.add(Short.parseShort((Integer.parseInt(values[1].split("/")[2]) - 1) + ""));
                        fVt.add(Short.parseShort((Integer.parseInt(values[2].split("/")[2]) - 1) + ""));
                        fVt.add(Short.parseShort((Integer.parseInt(values[3].split("/")[2]) - 1) + ""));
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        float data[] = new float[fV.size() * 3 + fVn.size() * 3 + fVt.size() * 3];
        indicesCount = fV.size();
        int k = 0;
        for (int i = 0; i < fV.size(); i++) {
            float vertex[] = v.get(fV.get(i));
            data[k++] = vertex[0] * size;
            data[k++] = vertex[1] * size;
            data[k++] = vertex[2] * size;
        }
        for (int i = 0; i < fVn.size(); i++) {
            float normal[] = vn.get(fVt.get(i));
            data[k++] = normal[0];
            data[k++] = normal[1];
            data[k++] = normal[2];
        }
        for (int i = 0; i < fVt.size(); i++) {
            float texture[] = vt.get(fVn.get(i) % vt.size());
            data[k++] = texture[0];
            data[k++] = texture[1];
            data[k++] = texture[2];
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
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(5);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, Float.SIZE / Byte.SIZE * 3 * fV.size());
        glVertexAttribPointer(5, 3, GL_FLOAT, false, 0, Float.SIZE / Byte.SIZE * 3 * fV.size() + Float.SIZE / Byte.SIZE * 3 * fVn.size());
        glBindVertexArray(0);
    }
    
    public Plane(float x, float y, float z, float size) {
        this(size);
        position.x = x;
        position.y = y;
        position.z = z;
    }
    
    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_FAN, 0, indicesCount);
        glBindVertexArray(0);
    }

    public Vector3f getPosition() {
        return position;
    }

}
