package com.aluracursos.screenmatch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aluracursos.screenmatch.dto.EpisodioDTO;
import com.aluracursos.screenmatch.dto.SerieDTO;
import com.aluracursos.screenmatch.service.SerieService;

@RestController
@RequestMapping("/series")
public class SerieController {
    @Autowired
    private SerieService servicio;

    @GetMapping()
    public List<SerieDTO> obtenerTodasLaSeries() {
        return servicio.obtenerTodasLaSeries();

    }

    @GetMapping("/lanzamientos")
    public List<SerieDTO> obtenerLanzamientosRecientes() {
        return servicio.obtenerLanzamientosRecientes();
    }

    @GetMapping("/top5")
    public List<SerieDTO> obtenerTop5Series() {
        return servicio.obtenerTop5Series();
    }

    @GetMapping("/{id}")
    public SerieDTO obtenerSeriePorId(@PathVariable Long id) {
        return servicio.obtenerPorId(id);
    }

    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDTO> obtenerTodasLasTemporadas(@PathVariable Long id) {
        return servicio.obtenerTodasLasTemporadas(id);
    }

    @GetMapping("/{id}/temporadas/{idTemporada}")
    public List<EpisodioDTO> obtenerTemporadaPorId(@PathVariable Long id, @PathVariable Long idTemporada) {
        return servicio.obtenerTemporadaPorId(id, idTemporada);
    }

    @GetMapping("/categoria/{genero}")
    public List<SerieDTO> obtenerSeriesPorGenero(@PathVariable String genero) {
        return servicio.obtenerSeriesPorGenero(genero);
    }
}
