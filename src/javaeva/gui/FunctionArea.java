package javaeva.gui;

/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 306 $
 *            $Date: 2007-12-04 14:22:52 +0100 (Tue, 04 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import wsi.ra.chart2d.DArea;
import wsi.ra.chart2d.DBorder;
import wsi.ra.chart2d.DPoint;
import wsi.ra.chart2d.DPointIcon;
import wsi.ra.chart2d.DPointSet;
import wsi.ra.chart2d.ScaledBorder;
import javaeva.server.go.individuals.AbstractEAIndividual;
import javaeva.server.go.mocco.paretofrontviewer.InterfaceRefPointListener;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class FunctionArea extends DArea implements Serializable {
    private InterfaceRefPointListener       m_RefPointListener;
    private JFileChooser                    m_FileChooser;
    private ArrayList                       m_PointSetContainer;
    private ScaledBorder                    m_Border;
    private boolean                         m_log = false;
    private boolean 						notifyNegLog = true;
    private int                             m_x;
    private int                             m_y;

    private DPointIcon                      m_CurrentPointIcon;
  /**
   *
   */
  public FunctionArea() {}

  /**
   *
   */
  public FunctionArea(String xname, String yname) {
    super();
    setPreferredSize(new Dimension(600, 500));
    setVisibleRectangle(1, 1, 100000, 1000);
    setAutoFocus(true);
    setMinRectangle(0, 0, 1, 1);
    //setAutoFocus(true);
    m_Border = new ScaledBorder();
    m_Border.x_label = xname; //"App. " + Name + " func. calls";
    m_Border.y_label = yname; //"fitness";
    setBorder(m_Border);
    setAutoGrid(true);
    setGridVisible(true);
    m_PointSetContainer = new ArrayList(20);
    //new DMouseZoom( this );
    addPopup();
    repaint();
    notifyNegLog = true;
  }

  /**
   *
   */
  public String getGraphInfo(int x, int y) {
    String ret = "";
    if ((m_PointSetContainer == null) || (m_PointSetContainer.size() == 0))
      return ret;
    int minindex = getNearestGraphIndex(x, y);
    ret = ((GraphPointSet) (m_PointSetContainer.get(minindex))).getInfoString();
    return ret;
  }

  /**
   *
   */
  public boolean isStatisticsGraph(int x, int y) {
    boolean ret = false;
    if ((m_PointSetContainer == null) || (m_PointSetContainer.size() == 0))
      return ret;
    int minindex = getNearestGraphIndex(x, y);
    ret = ((GraphPointSet) (m_PointSetContainer.get(minindex))).isStatisticsGraph();
    return ret;
  }


  /**
   *
   */
  private int getNearestGraphIndex(int x, int y) {
    // get index of nearest Graph
    double distmin = 10000000;
    int minindex = -1;
    DPoint point1 = getDMeasures().getDPoint(x, y);
    DPoint point2 = null;
    double dist = 0;
    for (int i = 0; i < m_PointSetContainer.size(); i++) {
      if (m_PointSetContainer.get(i)instanceof GraphPointSet) {
        GraphPointSet pointset = (GraphPointSet) (m_PointSetContainer.get(i));
        point2 = pointset.getNearestDPoint(point1);
        if (point2 == null)
          continue;
        if (point1 == null)
          System.err.println("point1 == null");

        dist = (point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y);
        //System.out.println("dist="+dist+"i="+i);
        if (dist < distmin) {
          distmin = dist;
          minindex = i;
        }
      }
    }
    return minindex;
  }

  /**
   *
   */
  private DPoint getNearestDPoint(int x, int y) {
    // get index of nearest Graph
    double distmin = 10000000;
    DPoint ret = null;
    DPoint point1 = getDMeasures().getDPoint(x, y);
    DPoint point2 = null;
    double dist = 0;
    for (int i = 0; i < m_PointSetContainer.size(); i++) {
      if (m_PointSetContainer.get(i)instanceof GraphPointSet) {
        GraphPointSet pointset = (GraphPointSet) (m_PointSetContainer.get(i));
        point2 = pointset.getNearestDPoint(point1);
        if (point2 == null)
          continue;
        dist = (point1.x - point2.x) * (point1.x - point2.x) + (point1.y - point2.y) * (point1.y - point2.y);
        //System.out.println("dist="+dist+"i="+i);
        if (dist < distmin) {
          distmin = dist;
          ret = point2;
        }
        if ((dist == distmin) && !(ret.getIcon()instanceof Chart2DDPointIconContent) && !(ret.getIcon()instanceof InterfaceSelectablePointIcon)) {
          distmin = dist;
          ret = point2;
        }
      }
    }
    return ret;
  }

  /**
   *
   */
  public void exportToAscii() {
    String[] s = null;
    for (int i = 0; i < m_PointSetContainer.size(); i++) {
      if (m_PointSetContainer.get(i)instanceof GraphPointSet) {
        GraphPointSet set = (GraphPointSet) m_PointSetContainer.get(i);
        String info = set.getInfoString();
        DPointSet pset = set.getConnectedPointSet();
        if (s == null) {
          s = new String[pset.getSize() + 1];
          s[0] = "calls";
        }
        s[0] = s[0] + " " + info;
        for (int j = 1; j < s.length; j++) {
          if (i == 0)
            s[j] = "" + pset.getDPoint(j - 1).x;
            try {
                s[j] = s[j] + " " + pset.getDPoint(j - 1).y;
            } catch (Exception e) {
                s[j] += " ";
            }
        }
      }
    }
    for (int j = 0; j < s.length; j++) {
      System.out.println("s=" + s[j]);
    }
    // todo: Steichert hat einfach einen default namen genommen
        SimpleDateFormat formatter = new SimpleDateFormat("E'_'yyyy.MM.dd'_'HH.mm.ss");
        String fname = "PlotExport_"+formatter.format(new Date());
//    System.out.println("Filename ??");
//    String fname = null;
//    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//    try {
//      fname = in.readLine();
//    } catch (Exception e) {
//      System.out.println("" + e.getMessage());
//    }
    try {
      File f = new File(fname + ".txt");
      f.createNewFile();
      PrintWriter Out = new PrintWriter(new FileOutputStream(f));
      for (int j = 0; j < s.length; j++)
        Out.println(s[j]);
      Out.flush();
      Out.close();
    } catch (Exception e) {
      System.err.println("Error:" + e.getMessage());
    }

  }

  /**
   *
   */
  public void setConnectedPoint(double x, double y, int GraphLabel) {
	  if (m_log == true && y <= 0.0) {
		  y = 1;
//		  y = Double.MIN_VALUE;
		  if (notifyNegLog) {
			  System.err.println("Warning: trying to plot value (" + x + "/" + y + ") with y < 0 in logarithmic mode! Setting y to " + y);
			  notifyNegLog = false;
		  }
	  }
	  getGraphPointSet(GraphLabel).addDPoint(x, y);

  }

  public void addGraphPointSet(GraphPointSet d) {
    this.m_PointSetContainer.add(d);
  }


  /**
   *
   */
  public void addGraph(int GraphLabel_1, int GraphLabel_2, boolean forceAdd) {
    getGraphPointSet(GraphLabel_1).addGraph(getGraphPointSet(GraphLabel_2), this.getDMeasures(), forceAdd);
    notifyNegLog = true;
  }

  /**
   *
   */
  public void clearGraph(int GraphLabel) {
    getGraphPointSet(GraphLabel).removeAllPoints();
    m_PointSetContainer.remove(getGraphPointSet(GraphLabel));
    repaint();
    notifyNegLog = true;
  }

  /**
   *
   */
  public void changeColorGraph(int GraphLabel) {
    Color col = getGraphPointSet(GraphLabel).getColor();
    if (col == Color.black)
      col = Color.red;
    else
    if (col == Color.red)
      col = Color.blue;
    else
    if (col == Color.blue)
      col = Color.red;
    else
    if (col == Color.red)
      col = Color.black;
    getGraphPointSet(GraphLabel).setColor(col);
    repaint();
  }

  /**
   *
   */
  public void clearGraph(int x, int y) {
    int index = getNearestGraphIndex(x, y);
    if (index == -1)
      return;
    int GraphLabel = ((GraphPointSet) (this.m_PointSetContainer.get(index))).getGraphLabel();
    clearGraph(GraphLabel);
  }

  /**
   *
   */
  public void changeColorGraph(int x, int y) {
    int index = getNearestGraphIndex(x, y);
    if (index == -1)
      return;
    int GraphLabel = ((GraphPointSet) (this.m_PointSetContainer.get(index))).getGraphLabel();
    changeColorGraph(GraphLabel);
  }

  /**
   *
   */
  public void removePoint(int x, int y) {
    DPoint point = getNearestDPoint(x, y);
    int index = getNearestGraphIndex(x, y);
    if (index == -1 || point == null)
      return;
    GraphPointSet pointset = (GraphPointSet) (this.m_PointSetContainer.get(index));
    pointset.removePoint(point);
  }

  /**
   *
   */
  public void setInfoString(int GraphLabel, String Info, float stroke) {
    getGraphPointSet(GraphLabel).setInfoString(Info, stroke);
  }

  /**
   *
   */
  public void clearAll() {
    this.removeAllDElements();
    for (int i = 0; i < m_PointSetContainer.size(); i++)
      ((GraphPointSet) (m_PointSetContainer.get(i))).removeAllPoints();
    m_PointSetContainer.clear();
    notifyNegLog = true;
  }

  /**
   *
   */
  private GraphPointSet getGraphPointSet(int GraphLabel) {
//  	System.out.println("looping through " + m_PointSetContainer.size() + " point sets...");
    for (int i = 0; i < m_PointSetContainer.size(); i++) {
      if (m_PointSetContainer.get(i) instanceof GraphPointSet) {
        GraphPointSet xx = (GraphPointSet) (m_PointSetContainer.get(i));
//        System.out.println("looking at "+xx.getGraphLabel());
        if (xx.getGraphLabel() == GraphLabel) {
//        	System.out.println("match!");
          return xx;
        }
      }
    }
    // create new GraphPointSet
    GraphPointSet NewPointSet = new GraphPointSet(GraphLabel, this);
//    System.out.println("adding new point set " + GraphLabel);
    //NewPointSet.setStroke(new BasicStroke( (float)1.0 ));
    //addGraphPointSet(NewPointSet); already done within GraphPointSet!!!
    return NewPointSet;
  }

  /**
   *
   */
  public void setUnconnectedPoint(double x, double y, int GraphLabel) {
	  if (m_log == true && y <= 0.0) {
		  if (notifyNegLog) {
			  System.err.println("Warning: trying to plot value (" + x + "/" + y + ") with y < 0 in logarithmic mode! Setting y to " + y );
			  notifyNegLog = false;
		  }
		  y = 1;
	  }
	  this.getGraphPointSet(GraphLabel).addDPoint(x, y);
	  this.getGraphPointSet(GraphLabel).setConnectedMode(false);
	  repaint();
  }

  Color[] Colors = new Color[] {Color.black, Color.red, Color.blue, Color.green,Color.magenta, Color.orange, Color.pink, Color.yellow};

  public void setGraphColor(int GraphLabel,int colorindex) {
    this.getGraphPointSet(GraphLabel).setColor(Colors[colorindex%Colors.length]);
  }

  /**
   * Returns the number of points within the graph of the given label.
   * 
   * @param index
   * @return
   */
  public int getPointCount(int label) {
	  return getGraphPointSet(label).getPointCount();
  }
  
  /**
   *
   */
  public int getContainerSize() {
    return m_PointSetContainer.size();
  }

  /**
   *
   */
  public DPointSet[] printPoints() {
    DPointSet[] ret = new DPointSet[m_PointSetContainer.size()];
    for (int i = 0; i < m_PointSetContainer.size(); i++) {
      System.out.println("");
      System.out.println("GraphPointSet No " + i);

      ret[i] = ((GraphPointSet) m_PointSetContainer.get(i)).printPoints();
    }
    return ret;

  }

  public DPointSet printPoints(int i) {
    //for (int i = 0; i < m_PointSetContainer.size();i++) {
    System.out.println("");
    System.out.println("GraphPointSet No " + i);

    return ((GraphPointSet) m_PointSetContainer.get(i)).printPoints();
    //}
  }

  /**
   *
   */
  public void toggleLog() {
    //System.out.println("ToggleLog log was: "+m_log);
    if (m_log == false) {
      setMinRectangle(0.001, 0.001, 1, 1);
      //setVisibleRectangle(  0.001, 0.001, 100000, 1000 );
      setYScale(new Exp());
      m_Border.setSrcdY(Math.log(10));
      ((java.text.DecimalFormat) m_Border.format_y).applyPattern("0.###E0");
      m_log = true;
    } else {
      m_log = false;
      setYScale(null);
      ScaledBorder buffer = m_Border;
      m_Border = new ScaledBorder();
      m_Border.x_label = buffer.x_label; //"App. " + Name + " func. calls";
      m_Border.y_label = buffer.y_label; //"fitness";
      setBorder(m_Border);
    }
    repaint();
  }

  /**
   * Causes all PointSets to interupt the connected painting at the
   * current position.
   */
  public void jump() {
    for (int i = 0; i < m_PointSetContainer.size(); i++)
      ((GraphPointSet) (m_PointSetContainer.get(i))).jump();
  }

  /**
   */
  public Object openObject() {
    if (m_FileChooser == null)
      createFileChooser();
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File selected = m_FileChooser.getSelectedFile();
      try {
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(selected)));
        Object obj = oi.readObject();
        oi.close();

        Object[] objects = (Object[]) obj;
        for (int i = 0; i < objects.length; i++) {
          GraphPointSet xx = ((GraphPointSet.SerPointSet) objects[i]).getGraphPointSet();
          xx.initGraph(this);
          addGraphPointSet(xx);
        }
        repaint();
        return obj;
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                                      "Couldn't read object: "
                                      + selected.getName()
                                      + "\n" + ex.getMessage(),
                                      "Open object file",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
    return null;
  }

  /**
   *
   */
  public void saveObject() {
    Object[] object = new Object[m_PointSetContainer.size()];
    for (int i = 0; i < m_PointSetContainer.size(); i++) {
      object[i] = ((GraphPointSet) m_PointSetContainer.get(i)).getSerPointSet();
    }
    if (m_FileChooser == null)
      createFileChooser();
    int returnVal = m_FileChooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File sFile = m_FileChooser.getSelectedFile();
      try {
        ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
        oo.writeObject(object);
        oo.close();
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this,
                                      "Couldn't write to file: "
                                      + sFile.getName()
                                      + "\n" + ex.getMessage(),
                                      "Save object",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   *
   */
  protected void createFileChooser() {
    m_FileChooser = new JFileChooser(new File("/resources"));
    m_FileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  }

  /** Add a popup menu for displaying certain information.
   */
  private void addPopup() {
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
        // do nothing
        } else {
          JPopupMenu GraphMenu = new JPopupMenu();
          m_x = e.getX();
          m_y = e.getY();

          // The first info element
          JMenuItem Info = new JMenuItem("Graph Info: " + getGraphInfo(e.getX(), e.getY()));
          Info.addActionListener(new ActionListener() {
            //
            public void actionPerformed(ActionEvent ee) {
              DPoint temp = FunctionArea.this.getDMeasures().getDPoint(FunctionArea.this.m_x, FunctionArea.this.m_y);
              DPointIcon icon1 = new DPointIcon() {
                public void paint(Graphics g) {
                  g.drawLine( -2, 0, 2, 0);
                  g.drawLine(0, 0, 0, 4);
                }

                public DBorder getDBorder() {
                  return new DBorder(4, 4, 4, 4);
                }
              };
              temp.setIcon(icon1);
              FunctionArea.this.addDElement(temp);
            }
          });
          GraphMenu.add(Info);
          if (m_RefPointListener != null) {
                DPoint temp = getDMeasures().getDPoint(m_x, m_y);
                JMenuItem refPoint = new JMenuItem("Reference Point:("+temp.x+"/"+temp.y+")");
                refPoint.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent ee) {
                      DPoint temp = getDMeasures().getDPoint(m_x, m_y);
                      double[] point = new double[2];
                      point[0] = temp.x;
                      point[1] = temp.y;
                      m_RefPointListener.refPointGiven(point);
                  }
                });
                GraphMenu.add(refPoint);
          }

          // darn this point is an empty copy !!
          DPoint point = getNearestDPoint(e.getX(), e.getY());

          if (point != null) {
            JMenuItem InfoXY = new JMenuItem("(" + point.x + "/" + point.y+")");
            Info.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ee) {
              }
            });
            GraphMenu.add(InfoXY);  

            if (point.getIcon()instanceof InterfaceSelectablePointIcon) {
                m_CurrentPointIcon              = point.getIcon();
                if (((InterfaceSelectablePointIcon)m_CurrentPointIcon).getSelectionListener() != null) {
                    JMenuItem select;
                    AbstractEAIndividual indy = ((InterfaceSelectablePointIcon)m_CurrentPointIcon).getEAIndividual();
                    if (indy.isMarked()) select = new JMenuItem("Deselect individual");
                    else                 select = new JMenuItem("Select individual");
                    select.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ee) {
                            ((InterfaceSelectablePointIcon)m_CurrentPointIcon).getSelectionListener().individualSelected(((InterfaceSelectablePointIcon)m_CurrentPointIcon).getEAIndividual());
                        }
                    });
                    GraphMenu.add(select);
                }
            }

            if (point.getIcon()instanceof InterfaceDPointWithContent) {
                m_CurrentPointIcon      = point.getIcon();
                JMenuItem drawIndy  = new JMenuItem("Show individual");
                drawIndy.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ee) {
                        ((InterfaceDPointWithContent)m_CurrentPointIcon).showIndividual();
                    }
                });
                GraphMenu.add(drawIndy);
            }

          }
          if (FunctionArea.this.m_PointSetContainer.size() > 0) {
            JMenuItem removeGraph = new JMenuItem("Remove graph");
            removeGraph.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ee) {
                clearGraph(FunctionArea.this.m_x, FunctionArea.this.m_y);
              }
            });
            GraphMenu.add(removeGraph);
          }
          if (FunctionArea.this.m_PointSetContainer.size() > 0) {
            JMenuItem changecolorGraph = new JMenuItem("Change color");
            changecolorGraph.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ee) {
                changeColorGraph(FunctionArea.this.m_x, FunctionArea.this.m_y);
              }
            });
            GraphMenu.add(changecolorGraph);
          }
          if (FunctionArea.this.m_PointSetContainer.size() > 0) {
            JMenuItem removePoint = new JMenuItem("Remove point");
            removePoint.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ee) {
                removePoint(FunctionArea.this.m_x, FunctionArea.this.m_y);
              }
            });
            GraphMenu.add(removePoint);
          }
//            if (isStatisticsGraph(e.getX(),e.getY())==true) {
//              if (getVar(e.getX(),e.getY())==false) {
//                JMenuItem showVar = new JMenuItem("Show varianz ");
//	        showVar.addActionListener(new ActionListener() {
//                //
//		public void actionPerformed(ActionEvent ee) {
//		  setVar(m_x,m_y,true);
//		}
//	      });
//	      GraphMenu.add(showVar);
//
//              }
//              else {
//                JMenuItem hideVar = new JMenuItem("Hide varianz ");
//	        hideVar.addActionListener(new ActionListener() {
//                  //
//		  public void actionPerformed(ActionEvent ee) {
//		    setVar(m_x,m_y,false);
//		  }
//                });
//                GraphMenu.add(hideVar);
//              }
//            }
          GraphMenu.show(FunctionArea.this, e.getX(), e.getY());
        }
      }
    });
  }

    /** This method allows to add a selection listner to the PointIcon
     * it should need more than one listener to this abstruse event
     * @param a The selection listener
     */
    public void addRefPointSelectionListener(InterfaceRefPointListener a) {
        this.m_RefPointListener = a;
    }

    /** This method returns the selection listner to the PointIcon
     * @return InterfaceSelectionListener
     */
    public InterfaceRefPointListener getRefPointSelectionListener() {
        return this.m_RefPointListener;
    }

    /** This method allows to remove the selection listner to the PointIcon
     */
    public void removeRefPointSelectionListeners() {
        this.m_RefPointListener = null;
    }

}