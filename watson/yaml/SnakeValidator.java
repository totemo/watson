package watson.yaml;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.yaml.snakeyaml.Yaml;

// ----------------------------------------------------------------------------
/**
 * Validates the DOM (Document Object Model) of a YAML document loaded by
 * SnakeYAML.
 */
public class SnakeValidator
{
  // --------------------------------------------------------------------------
  /**
   * For interactive testing.
   */
  public static void main(String[] args)
    throws Exception
  {
    testLoadBlocksYAML(args[0]);
  }

  // --------------------------------------------------------------------------
  /**
   * Test loading blocks.yml.
   */
  public static void testLoadBlocksYAML(String fileName)
    throws IOException
  {
    InputStream in = new BufferedInputStream(new FileInputStream(fileName));

    SnakeValidator validator = new SnakeValidator();

    // Describe a valid DOM for blocks.yml.
    MapValidatorNode block = new MapValidatorNode();
    block.addChild("id", new TypeValidatorNode(Integer.class));
    block.addChild("names", new ArrayValidatorNode(new TypeValidatorNode(
      String.class), false, 1, 10));

    block.addChild("data", new TypeValidatorNode(Integer.class, true, 0));
    block.addChild("lineWidth", new TypeValidatorNode(Double.class, true, 3.0));
    block.addChild("model", new TypeValidatorNode(String.class, true, "cuboid"));
    ArrayList<Object> defaultRGBA = new ArrayList<Object>(Arrays.asList(255, 0,
      255, 255));
    block.addChild("rgba", new ArrayValidatorNode(new TypeValidatorNode(
      Integer.class), false, 3, 4, defaultRGBA));
    ArrayList<Object> defaultBounds = new ArrayList<Object>(Arrays.asList(0.01,
      0.01, 0.01, 0.99, 0.99, 0.99));
    block.addChild("bounds", new ArrayValidatorNode(new TypeValidatorNode(
      Double.class), true, 6, 6, defaultBounds));

    ArrayValidatorNode blocks = new ArrayValidatorNode();
    blocks.setChild(block);
    MapValidatorNode root = new MapValidatorNode();
    root.addChild("blocks", blocks);
    validator.setRoot(root);

    ValidatorMessageSink stdoutSink = new ValidatorMessageSink()
    {
      @Override
      public void message(String text)
      {
        System.out.println(text);
      }
    };

    Object dom = validator.loadAndValidate(in, stdoutSink);

    try
    {
      BufferedWriter writer = new BufferedWriter(new FileWriter("out.dat"));
      try
      {
        // DumperOptions options = new DumperOptions();
        // options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml();
        yaml.dump(dom, writer);
      }
      finally
      {
        writer.close();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  } // main

  // --------------------------------------------------------------------------
  /**
   * Load the DOM from the specified InputStream and validate it.
   * 
   * @param in the InputStream.
   * @param sink used to output messages.
   */
  public Object loadAndValidate(InputStream in, ValidatorMessageSink sink)
  {
    Yaml yaml = new Yaml();
    Object root = yaml.load(in);
    validate(root, sink);
    return root;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the root of the {@link ValidatorNode} tree that reflects the expected
   * DOM hierarchy.
   * 
   * @param root the root.
   */
  public void setRoot(ValidatorNode root)
  {
    _root = root;
  }

  // --------------------------------------------------------------------------
  /**
   * Return root of the {@link ValidatorNode} tree that reflects the expected
   * DOM hierarchy.
   * 
   * @return the root of the {@link ValidatorNode} tree that reflects the
   *         expected DOM hierarchy.
   */
  public ValidatorNode getRoot()
  {
    return _root;
  }

  // --------------------------------------------------------------------------
  /**
   * Validate the specified DOM against getRoot().
   * 
   * @param dom the DOM.
   * @param sink used for error reporting.
   */
  public void validate(Object dom, ValidatorMessageSink sink)
  {
    getRoot().validate(dom, "", sink);
  }

  // --------------------------------------------------------------------------
  /**
   * The root of the {@link ValidatorNode} tree that reflects the expected DOM
   * hierarchy.
   */
  protected ValidatorNode _root;
} // class SnakeValidator