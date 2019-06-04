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

    private String[] datasets = {"data/alfred1.graphml",
            "data/alfred2.graphml",
            "data/bruno1.graphml",
            "data/bruno2.graphml",
            "data/butthole1.graphml",
            "data/butthole2.graphml",
            "data/emerald1.graphml",
            "data/emerald2.graphml",
            "data/emma1.graphml",
            "data/emma2.graphml",
            "data/ennon1.graphml",
            "data/ennon2.graphml",
            "data/erin1.graphml",
            "data/erin2.graphml",
            "data/fabricio1.graphml",
            "data/fabricio2.graphml",
            "data/goyman1.graphml",
            "data/goyman2.graphml",
            "data/hermano1.graphml",
            "data/hermano2.graphml",
            "data/jordan1.graphml",
            "data/jordan2.graphml",
            "data/jovin1.graphml",
            "data/jovin2.graphml",
            "data/jules1.graphml",
            "data/jules2.graphml",
            "data/kojin1.graphml",
            "data/kojin2.graphml",
            "data/kunal1.graphml",
            "data/kunal2.graphml",
            "data/leon1.graphml",
            "data/leon2.graphml",
            "data/lopes1.graphml",
            "data/lopes2.graphml",
            "data/luce1.graphml",
            "data/luce2.graphml",
            "data/malcolm1.graphml",
            "data/malcolm2.graphml",
            "data/michael1.graphml",
            "data/michael2.graphml",
            "data/michael3.graphml",
            "data/michael4.graphml",
            "data/niels1.graphml",
            "data/niels2.graphml",
            "data/rodrigo1.graphml",
            "data/rodrigo2.graphml",
            "data/ruan1.graphml",
            "data/ruan2.graphml",
            "data/tj1.graphml",
            "data/tj2.graphml",
            "data/will1.graphml",
            "data/will2.graphml",
            "data/zain1.graphml",
            "data/zain2.graphml",
            "data/mr. Swag1.graphml",
            "data/mr. Swag2.graphml",
            "data/rachel1.graphml",
            "data/rachel2.graphml"};

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
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

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
