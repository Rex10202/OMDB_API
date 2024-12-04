package com.aluracursos.screenmatch.principal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = System.getenv("API_KEY");
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulo
                    5 - Buscar top 5 series
                    6 - Buscar series por genero
                    7 - Buscar series por númoro de temporadas y evaluaciones
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios por serie
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarTop5Serie();
                    break;
                case 6:
                    buscarSeriePorGenero();
                    break;
                case 7:
                    buscarSeriePorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodioPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }

    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escriba el nombre de la serie que deseas buscar:");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi
                        .obtenerDatos(
                                URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }

    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        // datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        series = repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escriba el nombre de la serie que deseas buscar:");
        var nombreSerie = teclado.nextLine();
        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);
        if (serieBuscada.isPresent()) {
            System.out.println("La serie buscada es: " + serieBuscada.get());
        } else {
            System.out.println("No se ha encontrado la serie");
        }
    }

    private void buscarTop5Serie() {
        List<Serie> top5Series = repositorio.findTop5ByOrderByEvaluacionDesc();
        System.out.println("Top 5 series según la evaluación:");
        top5Series.forEach(s -> System.out.println("Serie: " + s.getTitulo() + " Evaluación: " + s.getEvaluacion()));
    }

    private void buscarSeriePorGenero() {
        System.out.println("Escribe el género de la serie que deseas buscar:");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie> seriesPorGenero = repositorio.findByGenero(categoria);
        System.out.println("Series por género: " + genero);
        seriesPorGenero.forEach(s -> System.out.println("Serie: " + s.getTitulo()));

    }

    private void buscarSeriePorTemporadaYEvaluacion() {
        try {
            System.out.println("Escribe el número de temporada para filtrar:");
            int totalTemporadas = teclado.nextInt();
            teclado.nextLine();

            System.out.println("Escribe la evaluación para filtrar:");

            String inputEvaluacion = teclado.nextLine();
            inputEvaluacion = inputEvaluacion.replace(",", ".");
            double evaluacion = Double.parseDouble(inputEvaluacion);

            List<Serie> seriesPorTemporadaYEvaluacion = repositorio.seriesPorTemporadaYEvalucion(totalTemporadas,
                    evaluacion);

            System.out.println("Series por temporada y evaluación:");
            if (seriesPorTemporadaYEvaluacion.isEmpty()) {
                System.out.println("No se encontraron series para la temporada " + totalTemporadas + " y evaluación "
                        + evaluacion);
            } else {
                seriesPorTemporadaYEvaluacion.forEach(
                        s -> System.out.println("Titulo: " + s.getTitulo() + " - Evaluación: " + s.getEvaluacion()));
            }
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingresa una evaluación válida en formato decimal. Ejemplo: 8.5 o 8,5.");
        } catch (InputMismatchException e) {
            System.out.println("Por favor, ingresa un número válido para la temporada.");
            teclado.nextLine();
        }
    }

    private void buscarEpisodioPorTitulo() {
        System.out.println("Escribe el título del episodio que deseas buscar:");
        var tituloEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(tituloEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Serie: %s Temporada: %s Episodio: %s Evaluación: %s\n",
                e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()));
    }

    private void buscarTop5Episodios() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.top5Episodios(serie);
            if (topEpisodios.isEmpty()) {
                System.out.println("No se encontraron episodios para la serie: " + serie.getTitulo());
            } else {
                topEpisodios.forEach(
                        e -> System.out.printf("Serie: %s Temporada: %s Titulo: %s Episodio: %s Evaluación: %s\n",
                                e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio(),
                                e.getEvaluacion()));
            }
        }
    }
}