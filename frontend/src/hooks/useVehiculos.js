import { useCallback, useEffect, useState } from 'react';
import { vehiculoService } from '../services/vehiculoService';

const FORM_INICIAL = {
    placa: '',
    propietario: '',
    telefono: '',
    tipo: 'Automóvil',
};

export function useVehiculos() {
    const [vehiculos, setVehiculos] = useState([]);
    const [form, setForm] = useState(FORM_INICIAL);
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);

    const cargar = useCallback(async () => {
        setCargando(true);

        try {
            const data = await vehiculoService.listar();
            setVehiculos(data);
            setError('');
        } catch (e) {
            setError(
                e.message || 'No fue posible consultar los vehículos.'
            );
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        cargar();
    }, [cargar]);

    function cambiar(e) {
        const { name, value } = e.target;

        setForm((actual) => ({
            ...actual,
            [name]: value,
        }));
    }

    async function registrar(e) {
        e.preventDefault();
        setMensaje('');
        setError('');
        setGuardando(true);

        try {
            const data = await vehiculoService.registrar(form);

            setMensaje(
                `Vehículo ${data.placa} registrado correctamente.`
            );

            setForm({ ...FORM_INICIAL });
            await cargar();
        } catch (e) {
            setError(e.message || 'No se pudo registrar el vehículo.');
        } finally {
            setGuardando(false);
        }
    }

    return {
        vehiculos,
        form,
        mensaje,
        error,
        cargando,
        guardando,
        cambiar,
        registrar,
        cargar,
    };
}