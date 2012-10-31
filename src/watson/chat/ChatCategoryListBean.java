package watson.chat;

import java.io.InputStream;
import java.util.List;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

// ----------------------------------------------------------------------------
/**
 * This class exists to facilitate loading the list of ChatCategory instances
 * using the SnakeYAML loader to load ChatCategory instances from a file.
 * 
 * All of the SnakeYAML examples of type safe lists show a bastardised JavaBeans
 * collection pattern where the whole List<> is get and set by a pair of
 * methods, rather than implementing proper JavaBeans indexed get and set
 * methods.
 */
public class ChatCategoryListBean
{
  // --------------------------------------------------------------------------
  /**
   * Load the list of ChatCategoryBean instances found under the "categories"
   * node in the specified stream.
   */
  public static ChatCategoryListBean load(InputStream in)
  {
    Constructor constructor = new Constructor(ChatCategoryListBean.class);
    TypeDescription categoriesDescription = new TypeDescription(
      ChatCategoryListBean.class);
    categoriesDescription.putListPropertyType("categories",
      ChatCategoryBean.class);
    constructor.addTypeDescription(categoriesDescription);
    Yaml yaml = new Yaml(constructor);
    return (ChatCategoryListBean) yaml.load(in);
  }

  // --------------------------------------------------------------------------
  /**
   * SnakeYAML's take on a JavaBeans setter for a sequential collection.
   */
  public void setCategories(List<ChatCategoryBean> categories)
  {
    _categories = categories;
  }

  // --------------------------------------------------------------------------
  /**
   * SnakeYAML's take on a JavaBeans getter for a sequential collection.
   */
  public List<ChatCategoryBean> getCategories()
  {
    return _categories;
  }

  // --------------------------------------------------------------------------
  /**
   * All of the ChatCategory instances.
   */
  private List<ChatCategoryBean> _categories;
} // class ChatCategoryListBean
