package project10;

import java.applet.Applet;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;
//make sure you've added Java3D libraries to your IDE's project libraries, else you'll get errors with the imports above.
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
    
/*Authored By: William Fletcher
* This program models and displays a mobius strip using Java3D.
* The display of this object is interactable with left-Click+Hold and drag,
* you can rotate the object. With the mouse wheel, you can zoom the camera in, and out.
* This code is release as Open-Souce, do whatever you want with it :)
*/

public class Project10 extends Applet {

        public static void main(String[] args) {
            new MainFrame(new Project10(), 800, 600);
        }

        @Override
        public void init() {
            GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
            Canvas3D canvas = new Canvas3D(gc);
            this.setLayout(new BorderLayout());
            this.add(canvas, BorderLayout.CENTER);
            SimpleUniverse su = new SimpleUniverse(canvas);
            su.getViewingPlatform().setNominalViewingTransform();
            BranchGroup bg = createSceneGraph();
            bg.compile();
            su.addBranchGraph(bg);
        }

        private BranchGroup createSceneGraph() {
            BranchGroup root = new BranchGroup();
            Shape3D shape = new Shape3D();
            shape.setGeometry(mobius().getIndexedGeometryArray());

            //Scaling transform
            Transform3D tr = new Transform3D();
            tr.setScale(0.5);

            //Spin transform group
            TransformGroup spin = new TransformGroup();
            spin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            spin.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            root.addChild(spin);

            //Set appearance
            Appearance ap = createTextureAppearance();
            ap.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
            PolygonAttributes.CULL_BACK, 0));

            //Set materials
            Material mat = new Material();
            mat.setLightingEnable(true);
            mat.setShininess(30);
            ap.setMaterial(mat);

            //Overarching Transform group
            TransformGroup tg = new TransformGroup(tr);
            tg.addChild(shape);
            spin.addChild(tg);
            shape.setAppearance(ap);

            //Set rotation
            MouseRotate rotator = new MouseRotate(spin);
            BoundingSphere bounds = new BoundingSphere();
            rotator.setSchedulingBounds(bounds);
            spin.addChild(rotator);

            //Set translation
            MouseTranslate translator = new MouseTranslate(spin);
            //translator.setSchedulingBounds(bounds);  this line was causing issues
            spin.addChild(translator);

            //Set zoom
            MouseWheelZoom zoom = new MouseWheelZoom(spin);
            zoom.setSchedulingBounds(bounds);
            spin.addChild(zoom);


            //Set background
            Background background = new Background(1.0f, 1.0f, 1.0f);
            background.setApplicationBounds(bounds);
            root.addChild(background);

            //Set lighting
            AmbientLight light = new AmbientLight(true, new Color3f(Color.BLACK));
            light.setInfluencingBounds(bounds);
            root.addChild(light);

            PointLight ptlight = new PointLight(new Color3f(Color.white),
                    new Point3f(0.5f, 0.5f, 1f),
                    new Point3f(1f, 0.2f, 0f));
            ptlight.setInfluencingBounds(bounds);
            root.addChild(ptlight);

            return root;
        }//Close branchgroup method

        //Create the Mobius shape
        private GeometryInfo mobius() {
            int rows = 100; //number of row points
            int columns = 100; //number of col points
            int faces = 4 * ((rows - 1) * (columns - 1)); //faces * points per face

            IndexedQuadArray iqa = new IndexedQuadArray(rows * columns,         
    GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2, faces);
            Point3d[] vertices = new Point3d[rows * columns];

    int index = 0;

            //Create vertices
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    double u = i * (4 * (Math.PI)) / (rows - 1);
                    double v = -0.3 + (j * (0.6 / (columns - 1)));
                    double x = (1 + v * Math.cos(u / 2)) * Math.cos(u);
                    double y = (1 + v * Math.cos(u / 2)) * Math.sin(u);
                    double z = v * Math.sin(u / 2);
                    vertices[index] = new Point3d(x, y, z);
                    index++;
                }//close nested for loop
            }//close for loop

            index = 0;
            //set texture coordinates
            TexCoord2f[] tex = new TexCoord2f[rows * columns];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    tex[index] = new TexCoord2f();
                    tex[index] = new TexCoord2f((rows-1-i) * 1f / rows, j * 1f / columns);
                    index++;
                }
            }

            iqa.setCoordinates(0, vertices);
            iqa.setTextureCoordinates(0, 0, tex);
            index = 0;

            //set index for coordinates
            int[] texIndices = new int[faces];
            for (int i = 0; i < rows - 1; i++) {
                for (int j = 0; j < columns - 1; j++) {
                    iqa.setCoordinateIndex(index, i * rows + j);
                    texIndices[index] = i * rows + j;
                    index++;
                    iqa.setCoordinateIndex(index, i * rows + j + 1);
                    texIndices[index] = i * rows + j + 1;
                    index++;
                    iqa.setCoordinateIndex(index, (i + 1) * rows + j + 1);
                    texIndices[index] = (i + 1) * rows + j + 1;
                    index++;
                    iqa.setCoordinateIndex(index, (i + 1) * rows + j);
                    texIndices[index] = (i + 1) * rows + j;
                    index++;
                }//close nested for loop
            }//close for loop

            iqa.setTextureCoordinateIndices(0, 0, texIndices);

            //create geometry info and generate normals for shape
            GeometryInfo gi = new GeometryInfo(iqa);
            NormalGenerator ng = new NormalGenerator();
            ng.generateNormals(gi);
            return gi;
        }

        Appearance createTextureAppearance() {
            Appearance ap = new Appearance();
            BufferedImage bi = new BufferedImage(1024, 128, 
            BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) bi.getGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, 1024, 128);
            g2.setFont(new Font("Serif", Font.BOLD, 36));
            g2.setColor(new Color(200, 0, 0));
            g2.drawString("Mobius Strip", 0, 100);
            ImageComponent2D image = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi);
            Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,         
            image.getWidth(), image.getHeight());
            texture.setImage(0, image);
            texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
            ap.setTexture(texture);

            //Combine Texture and Lighting
            TextureAttributes textatt = new TextureAttributes();
            textatt.setTextureMode(TextureAttributes.COMBINE);
            ap.setTextureAttributes(textatt);
            ap.setMaterial(new Material());
            return ap;

                }
    }
