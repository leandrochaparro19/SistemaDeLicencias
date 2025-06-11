package com.utn.santafe.gestion_licencias.service;

import com.utn.santafe.gestion_licencias.model.licencia.Licencia;
import com.utn.santafe.gestion_licencias.model.titular.ClaseLicencia;
import com.utn.santafe.gestion_licencias.model.titular.Titular;
import com.utn.santafe.gestion_licencias.repository.LicenciaRepository;
import com.utn.santafe.gestion_licencias.repository.TitularRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LicenciaServiceTest {

    @InjectMocks
    private LicenciaService licenciaService;

    @Mock
    private LicenciaRepository licenciaRepository;

    @Mock
    private TitularRepository titularRepository;

    /* ---------- FIXTURES ---------- */
    private Titular menor18;       // 18- años (primera y segunda vez)
    private Titular adulto40;      // 40 años (≤46)
    private Titular adulto55;      // 55 años (≤60)
    private Titular adulto65;      // 65 años (≤70)
    private Titular adulto75;      // 75+   (>70)
    private Titular prof25;        // 25 años — para clases profesionales

    @BeforeEach
    void init() {
        LocalDate hoy = LocalDate.now();

        menor18   = titularConEdad(18);   // edad exacta se calcula -1 día para evitar <17
        adulto40  = titularConEdad(40);
        adulto55  = titularConEdad(55);
        adulto65  = titularConEdad(65);
        adulto75  = titularConEdad(75);
        prof25    = titularConEdad(25);   // usaremos B y C/D/E

        menor18.setId(1L);
        adulto40.setId(2L);
        adulto55.setId(3L);
        adulto65.setId(4L);
        adulto75.setId(5L);
        prof25.setId(6L);
    }

    private Titular titularConEdad(int años) {
        Titular t = new Titular();
        t.setFechaNacimiento(LocalDate.now().minusYears(años));
        return t;
    }

    /* ========== 1. validarEdadMinima (error) ========== */
    @Test
    void emitirLicencia_edadMinima17_fail() {
        Titular niño16 = titularConEdad(16);
        niño16.setId(99L);
        when(titularRepository.findById(99L)).thenReturn(Optional.of(niño16));

        assertThatThrownBy(() ->
                licenciaService.emitirLicencia(99L, ClaseLicencia.B, null, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("edad mínima");
    }

    /* ========== 2. validarProfesional ========== */
    @Test
    void emitirLicencia_profesionalSinB1_fail() {
        when(titularRepository.findById(6L)).thenReturn(Optional.of(prof25));
        when(licenciaRepository.existsByTitularAndClaseAndFechaEmisionBefore(
                eq(prof25), eq(ClaseLicencia.B), any(LocalDate.class)))
                .thenReturn(false);               // no posee B1

        assertThatThrownBy(() ->
                licenciaService.emitirLicencia(6L, ClaseLicencia.C, null, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("clase B");
    }

    @Test
    void emitirLicencia_profesionalConB1_ok() {
        when(titularRepository.findById(6L)).thenReturn(Optional.of(prof25));
        when(licenciaRepository.existsByTitularAndClaseAndFechaEmisionBefore(
                eq(prof25), eq(ClaseLicencia.B), any(LocalDate.class)))
                .thenReturn(true);                // sí posee B1

        stubSinLicenciasPrevias(prof25, ClaseLicencia.C);

        Licencia lic = licenciaService.emitirLicencia(6L, ClaseLicencia.C, null, "admin");

        assertThat(lic.isVigente()).isTrue();
        assertThat(lic.getClase()).isEqualTo(ClaseLicencia.C);
    }

    /* ========== 3. emitirLicencia: primera vez & subsiguiente (<21) ========== */
    @Test
    void emitirLicencia_menorPrimeraVez() {
        when(titularRepository.findById(1L)).thenReturn(Optional.of(menor18));
        when(licenciaRepository.countByTitularAndClaseAndVigente(menor18, ClaseLicencia.A, true))
                .thenReturn(0L);      // primera vez
        stubSinLicenciasPrevias(menor18, ClaseLicencia.A);

        Licencia lic = licenciaService.emitirLicencia(1L, ClaseLicencia.A, null, "admin");

        assertThat(lic.getFechaVencimiento())
                .isEqualTo(calcularCumple(menor18, 1));
    }

    @Test
    void emitirLicencia_menorSubsecuente() {
        when(titularRepository.findById(1L)).thenReturn(Optional.of(menor18));
        when(licenciaRepository.countByTitularAndClaseAndVigente(menor18, ClaseLicencia.A, true))
                .thenReturn(1L);      // ya tiene una
        stubSinLicenciasPrevias(menor18, ClaseLicencia.A);

        Licencia lic = licenciaService.emitirLicencia(1L, ClaseLicencia.A, null, "admin");

        assertThat(lic.getFechaVencimiento())
                .isEqualTo(calcularCumple(menor18, 3));
    }

    /* ========== 4. emitirLicencia: cada franja etaria ========== */
    @Test void emitirLicencia_adulto40() { assertFranjas(adulto40, 2L, 5); } // ≤46 → 5
    @Test void emitirLicencia_adulto55() { assertFranjas(adulto55, 3L, 4); } // ≤60 → 4
    @Test void emitirLicencia_adulto65() { assertFranjas(adulto65, 4L, 3); } // ≤70 → 3
    @Test void emitirLicencia_adulto75() { assertFranjas(adulto75, 5L, 1); } //  >70 → 1

    private void assertFranjas(Titular t, long id, int añosEsperados) {
        when(titularRepository.findById(id)).thenReturn(Optional.of(t));
        when(licenciaRepository.countByTitularAndClaseAndVigente(t, ClaseLicencia.B, true))
                .thenReturn(0L);
        stubSinLicenciasPrevias(t, ClaseLicencia.B);

        Licencia lic = licenciaService.emitirLicencia(id, ClaseLicencia.B, null, "admin");

        assertThat(lic.getFechaVencimiento())
                .isEqualTo(calcularCumple(t, añosEsperados));
    }

    /* ========== 5. renovarLicencia (éxito + error) ========== */
    @Test
    void renovarLicencia_ok() {
        // Licencia anterior vigente
        Licencia anterior = new Licencia();
        anterior.setId(10L);
        anterior.setTitular(adulto40);
        anterior.setVigente(true);

        when(licenciaRepository.findById(10L)).thenReturn(Optional.of(anterior));
        when(licenciaRepository.findByTitularAndVigente(adulto40, true))
                .thenReturn(List.of(anterior));            // sólo la ‘anterior’
        when(licenciaRepository.countByTitularAndClaseAndVigente(
                adulto40, ClaseLicencia.B, true))
                .thenReturn(1L);                           // ya tenía clase B
        when(licenciaRepository.save(any(Licencia.class)))
                .thenAnswer(i -> i.getArgument(0));        // eco para ambas saves

        //  Ejecución
        Licencia nueva = licenciaService.renovarLicencia(
                10L,                // id licencia previa
                ClaseLicencia.B,    // misma clase
                "obs",              // observación
                "admin");           // usuario admin

        // Verificaciones
        assertThat(anterior.isVigente())
                .as("La licencia previa debe quedar inactiva")
                .isFalse();

        assertThat(nueva.isVigente())
                .as("La nueva licencia debe quedar vigente")
                .isTrue();

        // Se llama a save() dos veces: una para 'anterior', otra para 'nueva'
        verify(licenciaRepository, times(2)).save(any(Licencia.class));
    }

    @Test
    void renovarLicencia_noExiste() {
        when(licenciaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                licenciaService.renovarLicencia(999L, ClaseLicencia.B, null, "admin"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /* ========== 6. calcularVencimiento (caso día/mes ya pasó) ========== */
    @Test
    void calcularVencimiento_cumpleYaPaso() {
        Titular t = new Titular();
        // cumple 1° de enero
        t.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        LocalDate emite = LocalDate.of(2025, 1, 1); // un día después
        LocalDate venc  = licenciaService.calcularVencimiento(t, ClaseLicencia.B, emite, false);

        // debería caer al 1-ene-2030
        assertThat(venc).isEqualTo(LocalDate.of(2030, 1, 1));
    }

    /* ========== 7. listar/buscar/historial ========== */
    @Test
    void listarLicencias_devuelveRepositorio() {
        when(licenciaRepository.findAll()).thenReturn(List.of(new Licencia()));
        assertThat(licenciaService.listarLicencias()).hasSize(1);
    }

    @Test
    void buscarPorId_inexistente_lanzaExcepcion() {
        when(licenciaRepository.findById(123L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> licenciaService.buscarPorId(123L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void historialPorTitular_ok() {
        Licencia lic1 = new Licencia();
        when(titularRepository.findById(2L)).thenReturn(Optional.of(adulto40));
        when(licenciaRepository.findByTitularOrderByFechaEmisionDesc(adulto40))
                .thenReturn(List.of(lic1));

        assertThat(licenciaService.listarHistorialPorTitular(2L)).containsExactly(lic1);
    }

    /* ---------- helpers ---------- */

    /** evita ruido: no hay licencias vigentes previas */
    private void stubSinLicenciasPrevias(Titular t, ClaseLicencia c) {
        when(licenciaRepository.findByTitularAndVigente(t, true))
                .thenReturn(Collections.emptyList());
        when(licenciaRepository.save(any(Licencia.class)))
                .thenAnswer(i -> i.getArgument(0));
    }

    /** fecha = cumpleaños + 'años' */
    private LocalDate calcularCumple(Titular t, int años) {
        LocalDate base = LocalDate.now().plusYears(años)
                .withMonth(t.getFechaNacimiento().getMonthValue())
                .withDayOfMonth(t.getFechaNacimiento().getDayOfMonth());
        if (!base.isAfter(LocalDate.now())) base = base.plusYears(1);
        return base;
    }
}
