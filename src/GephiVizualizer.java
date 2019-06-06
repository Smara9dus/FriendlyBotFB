import com.itextpdf.text.PageSize;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

// TODO: Find a replacement for org.openide.util.Lookup since this keeps causing warnings
// TODO: Proper java code style cleanup
// TODO: Link to gephi-toolkit HeadlessSimple code that I started with
// TODO: Fix up the text printed to the console
// TODO: Find a different way to select a random dataset from the directory THAT REQUIRES LESS LINES!
// TODO: I think there may be node coordinates saved into some of the graphml files. Find a way to get rid of these OR ensure coordinates are randomized on import
// TODO: Capitalize first letter in the actual file names and get rid of this line
// TODO: Update probability of each layout. Make plain Fruchterman Reingold happen less and ForceAtlas2 work more
// TODO: Use specific range of iters for each layout option (Some make really small initial steps so the low range must be raised)
// TODO: Make a custom palette generator?
// TODO: Add rare option of color function containing 3 colors
// TODO: Make exported PDF square (figure out how to get a square PageSize)

public class GephiVizualizer {

    private String[] datasets = {"data/Alfred1.graphml",
            "data/Alfred2.graphml",
            "data/An1.graphml",
            "data/An2.graphml",
            "data/Bruno1.graphml",
            "data/Bruno2.graphml",
            "data/Butthole1.graphml",
            "data/Butthole2.graphml",
            "data/Denisa1.graphml",
            "data/Denisa2.graphml",
            "data/Emerald1.graphml",
            "data/Emerald2.graphml",
            "data/Emma1.graphml",
            "data/Emma2.graphml",
            "data/Ennon1.graphml",
            "data/Ennon2.graphml",
            "data/Erin1.graphml",
            "data/Erin2.graphml",
            "data/Fabricio1.graphml",
            "data/Fabricio2.graphml",
            "data/Goyman2.graphml",
            "data/Goyman2.graphml",
            "data/Gareth1.graphml",
            "data/Gareth2.graphml",
            "data/Hermano1.graphml",
            "data/Hermano2.graphml",
            "data/Ishmael1.graphml",
            "data/Ishmael2.graphml",
            "data/Jack1.graphml",
            "data/Jack2.graphml",
            "data/Jordan1.graphml",
            "data/Jordan2.graphml",
            "data/Jovin1.graphml",
            "data/Jovin2.graphml",
            "data/Joy1.graphml",
            "data/Joy2.graphml",
            "data/Jules1.graphml",
            "data/Jules2.graphml",
            "data/Justin1.graphml",
            "data/Justin2.graphml",
            "data/Kojin1.graphml",
            "data/Kojin2.graphml",
            "data/Kunal1.graphml",
            "data/Kunal2.graphml",
            "data/Leon1.graphml",
            "data/Leon2.graphml",
            "data/Lopes1.graphml",
            "data/Lopes2.graphml",
            "data/Luce1.graphml",
            "data/Luce2.graphml",
            "data/Malcolm1.graphml",
            "data/Malcolm2.graphml",
            "data/Mia1.graphml",
            "data/Mia2.graphml",
            "data/Michael1.graphml",
            "data/Michael2.graphml",
            "data/Michael3.graphml",
            "data/Michael4.graphml",
            "data/Mila1.graphml",
            "data/Mila2.graphml",
            "data/Mr. Squiggles1.graphml",
            "data/Mr. Squiggles2.graphml",
            "data/Mr. Swag1.graphml",
            "data/Mr. Swag2.graphml",
            "data/Niels1.graphml",
            "data/Niels2.graphml",
            "data/Quinten1.graphml",
            "data/Quinten2.graphml",
            "data/Rachel1.graphml",
            "data/Rachel2.graphml",
            "data/Rodrigo1.graphml",
            "data/Rodrigo2.graphml",
            "data/Ruan1.graphml",
            "data/Ruan2.graphml",
            "data/Semolini1.graphml",
            "data/Semolini2.graphml",
            "data/Tj1.graphml",
            "data/Tj2.graphml",
            "data/TotBot1.graphml",
            "data/TotBot2.graphml",
            "data/Vinicius1.graphml",
            "data/Vinicius2.graphml",
            "data/Will1.graphml",
            "data/Will2.graphml",
            "data/Zain1.graphml",
            "data/Zain2.graphml"};

    private Random rand;

    private Workspace workspace;

    private UndirectedGraph graph;
    private GraphModel graphModel;
    private PreviewModel model;
    private ImportController importController;
    private FilterController filterController;
    private AppearanceController appearanceController;
    private AppearanceModel appearanceModel;

    public String vizualize() {

        rand = new Random();

        String name = datasets[rand.nextInt(datasets.length)];

        setup();
        importData(name);
        filter();
        layout();
        color();
        size();
        preview();
        export();

        name = name.substring(5,name.length()-8).replaceAll("\\d","");

        return (name);
    }

    private void setup() {

        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        importController = Lookup.getDefault().lookup(ImportController.class);
        filterController = Lookup.getDefault().lookup(FilterController.class);
        appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        appearanceModel = appearanceController.getModel();
    }

    private void importData(String filepath) {

        //Import file
        Container container;
        try {
            System.out.println("Filepath: " + filepath);
            File file = new File(getClass().getResource(filepath).toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);   //Force UNDIRECTED
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        graph = graphModel.getUndirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        System.out.println("Data imported");
    }

    private void filter() {

        //Filter out floating nodes
        DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
        degreeFilter.init(graph);
        degreeFilter.setRange(new Range(1, Integer.MAX_VALUE));
        Query query = filterController.createQuery(degreeFilter);
        GraphView view = filterController.filter(query);
        graphModel.setVisibleView(view);

        //See visible graph stats
        UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
        System.out.println("Filtered Nodes: " + graphVisible.getNodeCount());
        System.out.println("Filtered Edges: " + graphVisible.getEdgeCount());

        System.out.println("Data filtered");
    }

    private void layout() {

        int iters = rand.nextInt(30)+100;
        int option = rand.nextInt(5);

        switch (option) {
            case 1: // ForceAtlas
                //System.out.println("Layout: ForceAtlas");
                ForceAtlasLayout forceAtlas = new ForceAtlasLayout(null);
                forceAtlas.setGraphModel(graphModel);
                forceAtlas.resetPropertiesValues();

                forceAtlas.initAlgo();
                for (int i = 0; i <  iters && forceAtlas.canAlgo(); i++) {
                    forceAtlas.goAlgo();
                }
                forceAtlas.endAlgo();
                break;
            case 2: //ForceAtlas2
                //System.out.println("Layout: ForceAtlas2");
                ForceAtlas2 forceAtlas2 = new ForceAtlas2(null);
                forceAtlas2.setGraphModel(graphModel);
                forceAtlas2.resetPropertiesValues();
                forceAtlas2.setStrongGravityMode(Boolean.TRUE);
                forceAtlas2.setGravity(0.2);

                forceAtlas2.initAlgo();
                for (int i = 0; i < iters && forceAtlas2.canAlgo(); i++) {
                    forceAtlas2.goAlgo();
                }
                forceAtlas2.endAlgo();
                break;
            case 3: // Fruchterman Reingold
                //System.out.println("Layout: Fruchterman Reingold");

                FruchtermanReingold fruchterman = new FruchtermanReingold(null);
                fruchterman.setGraphModel(graphModel);
                fruchterman.resetPropertiesValues();
                fruchterman.setGravity(100.0);
                fruchterman.setSpeed(10.0);

                fruchterman.initAlgo();
                for (int i = 0; i < iters && fruchterman.canAlgo(); i++) {
                    fruchterman.goAlgo();
                }
                fruchterman.endAlgo();
                break;
            case 4:
                //System.out.println("Layout: ForceAtlas2 + Fruchterman Reingold");
                ForceAtlas2 prefruchterman = new ForceAtlas2(null);
                prefruchterman.setGraphModel(graphModel);
                prefruchterman.resetPropertiesValues();
                prefruchterman.setStrongGravityMode(Boolean.TRUE);
                prefruchterman.setGravity(0.2);

                prefruchterman.initAlgo();
                for (int i = 0; i < 100 && prefruchterman.canAlgo(); i++) {
                    prefruchterman.goAlgo();
                }
                prefruchterman.endAlgo();


                FruchtermanReingold fruchterman2 = new FruchtermanReingold(null);
                fruchterman2.setGraphModel(graphModel);
                fruchterman2.resetPropertiesValues();
                fruchterman2.setGravity(100.0);
                fruchterman2.setSpeed(10.0);

                fruchterman2.initAlgo();
                for (int i = 0; i < iters && fruchterman2.canAlgo(); i++) {
                    fruchterman2.goAlgo();
                }
                fruchterman2.endAlgo();
                break;
            default: // Yifan Hu
                //System.out.println("Layout: Yifan Hu");
                YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
                layout.setGraphModel(graphModel);
                layout.resetPropertiesValues();
                layout.setOptimalDistance(200f);

                layout.initAlgo();
                for (int i = 0; i < iters && layout.canAlgo(); i++) {
                    layout.goAlgo();
                }
                layout.endAlgo();
                break;
        }

        System.out.println("Layout complete");
    }

    private void color() {
        Color color1, color2;
        int option = rand.nextInt(5);

        if(rand.nextInt(8)==1){ // Chance to choose grayscale
            int r1 = rand.nextInt(255);
            int r2 = rand.nextInt(255);
            color1 =  new Color(r1,r1,r1);
            color2 =  new Color(r2,r2,r2);
        } else {
            color1 = new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255));
            color2 = new Color(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255));
        }

        switch(option) {
            case 1: { // Rank color by Eigenvector Centrality
                //System.out.println("Color: Eigenvector Centrality");
                EigenvectorCentrality eigen = new EigenvectorCentrality();
                eigen.execute(graphModel);
                Column eigenColumn = graphModel.getNodeTable().getColumn(EigenvectorCentrality.EIGENVECTOR);
                Function eigenRanking = appearanceModel.getNodeFunction(graph, eigenColumn, RankingElementColorTransformer.class);
                RankingElementColorTransformer eigenTransformer = eigenRanking.getTransformer();
                eigenTransformer.setColors(new Color[]{color1, color2});
                eigenTransformer.setColorPositions(new float[]{0f, 1f});
                appearanceController.transform(eigenRanking);
                break;
            }
            case 2: { // Rank color by Centrality
                //System.out.println("Color: Centrality");
                GraphDistance distance = new GraphDistance();
                distance.setDirected(false);
                distance.execute(graphModel);
                Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
                Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingElementColorTransformer.class);
                RankingElementColorTransformer centralityTransformer = centralityRanking.getTransformer();
                centralityTransformer.setColors(new Color[]{color1, color2});
                centralityTransformer.setColorPositions(new float[]{0f, 1f});
                appearanceController.transform(centralityRanking);
                break;
            }
            case 3: { // Rank color by PageRank
                //System.out.println("Color: PageRank");
                PageRank pageRank = new PageRank();
                pageRank.execute(graphModel);
                Column pageRankColumn = graphModel.getNodeTable().getColumn(PageRank.PAGERANK);
                Function pageRankRanking = appearanceModel.getNodeFunction(graph, pageRankColumn, RankingElementColorTransformer.class);
                RankingElementColorTransformer pageRankTransformer = pageRankRanking.getTransformer();
                pageRankTransformer.setColors(new Color[]{color1, color2});
                pageRankTransformer.setColorPositions(new float[]{0f, 1f});
                appearanceController.transform(pageRankRanking);
                break;
            }
            case 4: { // Rank color by degree
                //System.out.println("Color: Degree");
                Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingElementColorTransformer.class);
                RankingElementColorTransformer degreeTransformer = degreeRanking.getTransformer();
                degreeTransformer.setColors(new Color[]{ color1, color2});
                degreeTransformer.setColorPositions(new float[]{0f, 1f});
                appearanceController.transform(degreeRanking);
                break;
            }
            default: {
                //System.out.println("Color: Modularity Class");
                Modularity modularity = new Modularity();
                modularity.setUseWeight(Boolean.FALSE);
                modularity.setRandom(Boolean.TRUE);
                modularity.setResolution(5.0);
                modularity.execute(graphModel);
                Column modColumn = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
                Function func2 = appearanceModel.getNodeFunction(graph, modColumn, PartitionElementColorTransformer.class);
                Partition partition2 = ((PartitionFunction) func2).getPartition();
                Palette palette2 = PaletteManager.getInstance().randomPalette(partition2.size());
                partition2.setColors(palette2.getColors());
                appearanceController.transform(func2);
            }
        }

        System.out.println("Color transform complete");
    }

    private void size() {

        int minSize = rand.nextInt(2) + 1;
        int maxSize = rand.nextInt(12) + 8;

        int option = rand.nextInt(5);

        switch(option) {
            case 1: { // Rank size by Eigenvector Centrality
                //System.out.println("Size: Eigenvector Centrality");
                EigenvectorCentrality eigen = new EigenvectorCentrality();
                eigen.execute(graphModel);
                Column eigenColumn = graphModel.getNodeTable().getColumn(EigenvectorCentrality.EIGENVECTOR);
                Function eigenRanking = appearanceModel.getNodeFunction(graph, eigenColumn, RankingNodeSizeTransformer.class);
                RankingNodeSizeTransformer eigenTransformer = eigenRanking.getTransformer();
                eigenTransformer.setMinSize(minSize);
                eigenTransformer.setMaxSize(maxSize);
                appearanceController.transform(eigenRanking);
                break;
            }
            case 2: { // Rank size by Centrality
                //System.out.println("Size: Centrality");
                GraphDistance distance = new GraphDistance();
                distance.setDirected(false);
                distance.execute(graphModel);
                Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
                Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingNodeSizeTransformer.class);
                RankingNodeSizeTransformer centralityTransformer = centralityRanking.getTransformer();
                centralityTransformer.setMinSize(minSize);
                centralityTransformer.setMaxSize(maxSize);
                appearanceController.transform(centralityRanking);
                break;
            }
            case 3: { // Rank size by PageRank
                //System.out.println("Size: PageRank");
                PageRank pageRank = new PageRank();
                pageRank.execute(graphModel);
                Column pageRankColumn = graphModel.getNodeTable().getColumn(PageRank.PAGERANK);
                Function pageRankRanking = appearanceModel.getNodeFunction(graph, pageRankColumn, RankingNodeSizeTransformer.class);
                RankingNodeSizeTransformer pageRankTransformer = pageRankRanking.getTransformer();
                pageRankTransformer.setMinSize(minSize);
                pageRankTransformer.setMaxSize(maxSize);
                appearanceController.transform(pageRankRanking);
                break;
            }
            case 4: { // Rank size by degree
                //System.out.println("Size: Degree");
                Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingNodeSizeTransformer.class);
                RankingNodeSizeTransformer degreeTransformer = degreeRanking.getTransformer();
                degreeTransformer.setMinSize(minSize);
                degreeTransformer.setMaxSize(maxSize);
                appearanceController.transform(degreeRanking);
                break;
            }
            default: {
                //System.out.println("No Size Ranking");
                break;
            }
        }

        System.out.println("Size transform complete");
    }

    private void preview() {

        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.FALSE);
        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(EdgeColor.Mode.MIXED));
        model.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH, 0);

        // Edge type (50% Straight, 50% Curved)
        if (rand.nextBoolean()) {
            model.getProperties().putValue(PreviewProperty.EDGE_CURVED, true);
        } else {
            model.getProperties().putValue(PreviewProperty.EDGE_CURVED, false);
        }

        // Graph style (10% Invisible nodes, 40% Random opacity, 30% Normal nodes, 20% Normal nodes/edges)
        int option = rand.nextInt(10);
        if (option < 2) {
            model.getProperties().putValue(PreviewProperty.NODE_OPACITY, 0.01);
            model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, rand.nextFloat()*100);
            model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, rand.nextFloat()*14.9+.1f);
        } else if (option < 5) {
            model.getProperties().putValue(PreviewProperty.NODE_OPACITY, rand.nextFloat()*100);
            model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, rand.nextFloat()*100);
            model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, rand.nextFloat()*0.9+.1f);
        } else if (option < 8) {
            model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, rand.nextFloat()*90 + 10);
            model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, rand.nextFloat()*0.9+.1f);
        } else {
            model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, rand.nextFloat()*0.9+.1f);
        }

        // Background color (20% White, 20% Random, 60% Black)
        option = rand.nextInt(5);
        if (option < 1) {
            model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
        } else if (option < 2) {
            Color c = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
            model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, c);
        } else {
            model.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
        }

        System.out.println("Exporting...");
    }

    private void export() {

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);

        PNGExporter exp = new PNGExporter();
        exp.setWorkspace(workspace);
        exp.setWidth(2048);
        exp.setHeight(2048);

        // export png
        try {
            File newPng = new File("/Users/EBT/Desktop/FriendlyBotFB/recent.png");
            ec.exportFile(newPng, exp);
            System.out.println("Png Exported");
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        PDFExporter exp2 = new PDFExporter();
        exp2.setPageSize(PageSize.NOTE);
        exp2.setWorkspace(workspace);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String filename = dateFormat.format(date);

        // export pdf
        try {
            File newPdf = new File("/Users/EBT/Desktop/FriendlyBotFB/pdfs/" + filename + ".pdf");
            ec.exportFile(newPdf, exp2);
            System.out.println("Pdf Exported");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
