
import java.sql.*;
import java.util.Scanner;

public class ExoJDBC {

    // Paramètres de connexion
    private static final String URL = "jdbc:mysql://localhost:3306/atelier?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        Scanner entrer = new Scanner(System.in);
        // Charger le driver (optionnel en JDBC moderne, mais utile pédagogiquement)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL introuvable. Vérifier l'import du .jar.");
            e.printStackTrace();
            return;
        }

        // Connexion + Statement
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement()) {

            System.out.println("Connexion MySQL OK.");

            // Réinitialiser la table
            stmt.executeUpdate("DROP TABLE IF EXISTS DevData");
            stmt.executeUpdate(
                    "CREATE TABLE DevData ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "Developpeurs VARCHAR(32) NOT NULL, "
                    + "Jour VARCHAR(16) NOT NULL, "
                    + "NbScripts INT NOT NULL,"
                    + "UNIQUE (Developpeurs, jour),"
                    + "CONSTRAINT chk_scripts CHECK (NbScripts >=0)"
                    + ")"
            );

            // Données de test
            stmt.executeUpdate("INSERT INTO DevData VALUES (1,'ALAMI', 'Lundi', 1)");
            stmt.executeUpdate("INSERT INTO DevData VALUES (2,'WAFI', 'Lundi', 2)");
            stmt.executeUpdate("INSERT INTO DevData VALUES (3,'SLAMI', 'Mardi', 9)");
            stmt.executeUpdate("INSERT INTO DevData VALUES (4,'ALAMI', 'Mardi', 3)");
            stmt.executeUpdate("INSERT INTO DevData VALUES (5,'WAFI', 'Mardi', 4)");
            stmt.executeUpdate("INSERT INTO DevData VALUES (6,'SLAMI', 'Mercredi', 2)");

            System.out.println("Table créée + données insérées.");

            // Statistique 1 : max par jour
            System.out.println("\n--- Max scripts par jour ---");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT Jour, Developpeurs, MAX(NbScripts) AS MaxScripts "
                    + "FROM DevData GROUP BY Jour"
            )) {
                while (rs.next()) {
                    String jour = rs.getString("Jour");
                    String dev = rs.getString("Developpeurs");
                    int max = rs.getInt("MaxScripts");
                    System.out.println(jour + " | " + dev + " | " + max);
                }
            }

            // Statistique 2 : classement par total décroissant
            System.out.println("\n--- Classement des développeurs (total scripts) ---");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT Developpeurs, SUM(NbScripts) AS Total "
                    + "FROM DevData GROUP BY Developpeurs ORDER BY Total DESC"
            )) {
                while (rs.next()) {
                    String dev = rs.getString("Developpeurs");
                    int total = rs.getInt("Total");
                    System.out.println(dev + " | " + total);
                }
            }

            // Statistique 3 : total semaine
            System.out.println("\n--- Total scripts semaine ---");
            try (ResultSet rs = stmt.executeQuery("SELECT SUM(NbScripts) AS TotalSemaine FROM DevData")) {
                if (rs.next()) {
                    System.out.println("Total semaine : " + rs.getInt("TotalSemaine"));
                }
            }
            System.out.println("\n---moyenne des scripts par jour---");
            String MoyenneScripts = "SELECT jour,AVG(NbScripts) AS Moyenne FROM DevData GROUP BY jour";
            try (ResultSet rs = stmt.executeQuery(MoyenneScripts)) {
                while (rs.next()) {
                    String jour = rs.getString("Jour");
                    double moyenne = rs.getDouble("Moyenne");
                    System.out.printf("%s | %.2f scripts\n", jour, moyenne);
                }
            }

            // Statistique 4 : total pour un développeur (PreparedStatement)
            System.out.println("\n--- Total scripts pour un développeur (PreparedStatement) ---");
            System.out.println("Entrer le nom du devllopeur : ");
            String devRecherche = entrer.nextLine();

            String sql = "SELECT SUM(NbScripts) AS TotalDev FROM DevData WHERE Developpeurs = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, devRecherche);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int totalDev = rs.getInt("TotalDev");
                        System.out.println("Total pour " + devRecherche + " : " + totalDev);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : vérifier MySQL (base, user/password, port).");
            e.printStackTrace();
        }
    }
}
