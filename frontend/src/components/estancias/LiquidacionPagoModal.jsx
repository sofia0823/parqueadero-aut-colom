
import { useEffect, useState } from "react";
import {
    Banknote,
    CheckCircle2,
    CircleAlert,
    Clock3,
    CreditCard,
    LoaderCircle,
    X,
} from "lucide-react";

import { pagoService } from "../../services/pagoService";

export default function LiquidacionPagoModal({
    estancia,
    onCerrar,
    onPagoConfirmado,
}) {
    const [liquidacion, setLiquidacion] = useState(null);
    const [metodo, setMetodo] = useState("EFECTIVO");
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let activo = true;

        async function cargarLiquidacion() {
            setCargando(true);
            setError("");
            setLiquidacion(null);

            try {
                const datos = await pagoService.calcularLiquidacion(
                    estancia.id
                );

                if (activo) {
                    setLiquidacion(datos);
                }
            } catch (e) {
                if (activo) {
                    setError(
                        e.message ||
                        "No fue posible calcular la liquidación."
                    );
                }
            } finally {
                if (activo) {
                    setCargando(false);
                }
            }
        }

        cargarLiquidacion();

        return () => {
            activo = false;
        };
    }, [estancia.id]);

    function moneda(valor) {
        return new Intl.NumberFormat("es-CO", {
            style: "currency",
            currency: "COP",
            maximumFractionDigits: 0,
        }).format(Number(valor || 0));
    }

    function fecha(valor) {
        if (!valor) return "—";

        const resultado = new Date(valor);

        if (Number.isNaN(resultado.getTime())) {
            return valor;
        }

        return resultado.toLocaleString("es-CO", {
            dateStyle: "medium",
            timeStyle: "short",
        });
    }

    async function confirmarPago() {
        if (!liquidacion || guardando || cargando) return;

        setError("");
        setGuardando(true);

        try {
            const resultado = await pagoService.confirmarPago(
                estancia.id,
                { metodo }
            );

            // El servidor ya confirmó el pago y la salida.
            // El callback se encarga de actualizar la página.
            await onPagoConfirmado(resultado);
        } catch (e) {
            setError(
                e.message ||
                "No fue posible confirmar el pago."
            );
        } finally {
            setGuardando(false);
        }
    }

    function cerrarModal() {
        if (!guardando) {
            onCerrar();
        }
    }

    const placa =
        liquidacion?.placa ||
        estancia.vehiculo?.placa ||
        "—";

    return (
        <div
            className="modal d-block"
            tabIndex="-1"
            role="dialog"
            aria-modal="true"
            aria-labelledby="tituloLiquidacion"
            style={{ backgroundColor: "rgba(0, 0, 0, 0.5)" }}
            onMouseDown={(evento) => {
                if (
                    evento.target === evento.currentTarget &&
                    !guardando
                ) {
                    cerrarModal();
                }
            }}
        >
            <div className="modal-dialog modal-dialog-centered modal-dialog-scrollable">
                <div className="modal-content border-0 shadow">
                    <div className="modal-header">
                        <div>
                            <h5
                                className="modal-title fw-bold"
                                id="tituloLiquidacion"
                            >
                                Liquidación y pago
                            </h5>

                            <p className="text-secondary small mb-0">
                                Vehículo: {placa}
                            </p>
                        </div>

                        <button
                            type="button"
                            className="btn-close"
                            aria-label="Cerrar"
                            onClick={cerrarModal}
                            disabled={guardando}
                        />
                    </div>

                    <div className="modal-body">
                        {error && (
                            <div
                                className="alert alert-danger d-flex align-items-start gap-2"
                                role="alert"
                            >
                                <CircleAlert
                                    size={20}
                                    className="flex-shrink-0"
                                />
                                <div className="flex-grow-1">
                                    <strong>Atención</strong>
                                    <div>{error}</div>
                                </div>
                            </div>
                        )}

                        {cargando ? (
                            <div className="text-center py-5">
                                <LoaderCircle
                                    size={34}
                                    className="text-primary"
                                />

                                <p className="text-secondary mt-3 mb-0">
                                    Calculando el valor de la estancia...
                                </p>
                            </div>
                        ) : liquidacion ? (
                            <>
                                <div className="rounded-3 bg-light p-3 mb-4">
                                    <div className="d-flex justify-content-between align-items-start gap-3 mb-3">
                                        <span className="text-secondary">
                                            Fecha de entrada
                                        </span>
                                        <span className="fw-semibold text-end">
                                            {fecha(liquidacion.entrada)}
                                        </span>
                                    </div>

                                    <div className="d-flex justify-content-between align-items-center gap-3 mb-3">
                                        <span className="text-secondary d-flex align-items-center gap-2">
                                            <Clock3 size={17} />
                                            Tiempo transcurrido
                                        </span>
                                        <span className="fw-semibold">
                                            {liquidacion.minutosEstancia} min
                                        </span>
                                    </div>

                                    <div className="d-flex justify-content-between gap-3 mb-3">
                                        <span className="text-secondary">
                                            Horas facturadas
                                        </span>
                                        <span className="fw-semibold">
                                            {liquidacion.horasFacturadas}
                                        </span>
                                    </div>

                                    <div className="d-flex justify-content-between gap-3">
                                        <span className="text-secondary">
                                            Tarifa por hora
                                        </span>
                                        <span className="fw-semibold">
                                            {moneda(liquidacion.tarifaPorHora)}
                                        </span>
                                    </div>

                                    <hr />

                                    <div className="d-flex justify-content-between align-items-center gap-3">
                                        <span className="fw-bold">
                                            Total a pagar
                                        </span>
                                        <span className="fs-3 fw-bold text-primary">
                                            {moneda(liquidacion.total)}
                                        </span>
                                    </div>
                                </div>

                                <label
                                    htmlFor="metodoPago"
                                    className="form-label fw-semibold"
                                >
                                    Método de pago
                                </label>

                                <div className="d-grid gap-2">
                                    {[
                                        {
                                            valor: "EFECTIVO",
                                            texto: "Efectivo",
                                            Icono: Banknote,
                                        },
                                        {
                                            valor: "TARJETA",
                                            texto: "Tarjeta",
                                            Icono: CreditCard,
                                        },
                                        {
                                            valor: "TRANSFERENCIA",
                                            texto: "Transferencia",
                                            Icono: CheckCircle2,
                                        },
                                    ].map(({ valor, texto, Icono }) => (
                                        <button
                                            key={valor}
                                            type="button"
                                            className={`btn d-flex align-items-center gap-3 text-start ${
                                                metodo === valor
                                                    ? "btn-primary"
                                                    : "btn-outline-secondary"
                                            }`}
                                            onClick={() => setMetodo(valor)}
                                            disabled={guardando}
                                            aria-pressed={metodo === valor}
                                        >
                                            <Icono size={20} />
                                            <span className="flex-grow-1">
                                                {texto}
                                            </span>
                                            {metodo === valor && (
                                                <CheckCircle2 size={18} />
                                            )}
                                        </button>
                                    ))}
                                </div>

                                <div className="form-text mt-3">
                                    La tarifa es de $2.000 COP por cada hora
                                    iniciada. El servidor calcula el cobro
                                    definitivo al confirmar.
                                </div>
                            </>
                        ) : (
                            <p className="text-secondary mb-0">
                                No se pudo cargar la liquidación. Puedes
                                cerrar esta ventana e intentarlo nuevamente.
                            </p>
                        )}
                    </div>

                    <div className="modal-footer">
                        <button
                            type="button"
                            className="btn btn-outline-secondary"
                            onClick={cerrarModal}
                            disabled={guardando}
                        >
                            <X size={16} className="me-1" />
                            Cancelar
                        </button>

                        <button
                            type="button"
                            className="btn btn-primary d-inline-flex align-items-center gap-2"
                            onClick={confirmarPago}
                            disabled={
                                cargando ||
                                !liquidacion ||
                                guardando
                            }
                        >
                            {guardando ? (
                                <>
                                    <LoaderCircle size={17} />
                                    Procesando...
                                </>
                            ) : (
                                <>
                                    <CheckCircle2 size={17} />
                                    Confirmar pago y salida
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
