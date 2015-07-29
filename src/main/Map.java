package main;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 *
 * @author d
 */
public class Map {
    
    private TerrainQuad terrain;
    private AssetManager assetManager;
    private Node rootNode;
    Material terrain_mat;
    
    public Map(AssetManager assetManager, Node rootNode) {
        
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        terrain_mat = new Material(assetManager,
                "Common/MatDefs/Terrain/Terrain.j3md");
        terrain_mat.setTexture("Alpha", assetManager.loadTexture(
                "Textures/alphamap3.png"));
        /**/
        Texture lava = assetManager.loadTexture(
                "Textures/lavatex.jpg");
        lava.setWrap(WrapMode.Repeat);
        terrain_mat.setTexture("Tex1", lava);
        terrain_mat.setFloat("Tex1Scale", 32f);
        
        Texture rock = assetManager.loadTexture(
                "Textures/rocktex.jpg");
        rock.setWrap(WrapMode.Repeat);
        terrain_mat.setTexture("Tex2", rock);
        terrain_mat.setFloat("Tex2Scale", 64f);
        
        Texture dirt = assetManager.loadTexture(
                "Textures/dirttex.jpg");
        dirt.setWrap(WrapMode.Repeat);
        terrain_mat.setTexture("Tex3", dirt);
        terrain_mat.setFloat("Tex3Scale", 64f);
        
        AbstractHeightMap heightmap = null;
        Texture heightmapImage = assetManager.loadTexture(
                "Textures/terrain3.jpg");
        heightmap = new ImageBasedHeightMap(heightmapImage.getImage());
        heightmap.load();
        
        int patchSize = 65;
        
        terrain = new TerrainQuad("my terrain", patchSize,
                513, heightmap.getHeightMap());
        terrain.setMaterial(terrain_mat);
        terrain.setLocalTranslation(0, -200, 0);
        terrain.setLocalScale(2f, 1f, 2f);  
        rootNode.attachChild(terrain);
    }
    

   
}
