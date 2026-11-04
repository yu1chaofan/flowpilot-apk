/*
 * CPython 3.12+ compatibility shim for Cython-generated code.
 *
 * Cython's SetNameInClass utility emits a call to
 * _PyDict_SetItem_KnownHash() guarded by
 *
 *     #if CYTHON_COMPILING_IN_CPYTHON && PY_VERSION_HEX >= 0x030500A1
 *
 * That symbol lived in cpython/dictobject.c and was declared only in the
 * private header pycore_dict.h, which p4a's python3 build does not install
 * (it ships include/python3.14 only, without the pycore_* headers). The
 * result is a hard compile failure:
 *
 *     capnp/lib/capnp.cpp:109876:7: error: use of undeclared identifier
 *       '_PyDict_SetItem_KnownHash'
 *
 * The fix is deliberately conservative: provide a *macro* that forwards to
 * the public PyDict_SetItem(). We lose only the pre-computed-hash fast path
 * for exact-dict inserts, which is a negligible cost for pycapnp (its hot
 * path is capnp serialization, not dict building), and we get a definition
 * that is correct on every CPython version.
 *
 * We do not try to declare the real private symbol: it is no longer exported
 * from libpython in 3.13+, so a declaration would link on some versions and
 * fail on others.
 *
 * This header must be force-included (-include) so it is seen before
 * capnp.cpp uses the macro.
 */
#ifndef PYCAPNP_CYTHON_COMPAT_H
#define PYCAPNP_CYTHON_COMPAT_H

#include <Python.h>

#if PY_VERSION_HEX >= 0x030C0000
/* Python 3.12+ : _PyDict_SetItem_KnownHash is internal / removed. */
#ifndef _PyDict_SetItem_KnownHash
#define _PyDict_SetItem_KnownHash(d, k, v, h) \
  PyDict_SetItem((PyObject *)(d), (PyObject *)(k), (PyObject *)(v))
#endif
#endif

#if PY_VERSION_HEX >= 0x030C0000
/*
 * Cython 3's module-init boilerplate calls _PyInterpreterState_GetConfig()
 * to read the default string encoding:
 *
 *   PyConfig *config = (PyConfig *)_PyInterpreterState_GetConfig(
 *       PyInterpreterState_Get());
 *
 * Also a private symbol (pycore_interp.h), also absent from p4a's headers.
 * We return NULL: Cython then falls back to the UTF-8 default encoding, which
 * is the behaviour we want for capnp. See the note at the definition below for
 * why we do not read interp->config.
 */
#ifndef _PyInterpreterState_GetConfig
/*
 * Signature is `const PyConfig *_PyInterpreterState_GetConfig(PyInterpreterState *)`.
 * We return NULL rather than poking at interp->config: the field is only
 * visible through the cpython/ headers and its offset is not part of any
 * stable ABI, so reading it would trade a compile error for silent memory
 * corruption. NULL is an accepted input for Cython's use -- it then falls
 * back to the UTF-8 default encoding, which is what we want anyway.
 */
static inline const PyConfig *_pycapnp_interp_get_config(PyInterpreterState *interp) {
  (void)interp;
  return NULL;
}
#define _PyInterpreterState_GetConfig(interp) _pycapnp_interp_get_config(interp)
#endif
#endif

#endif /* PYCAPNP_CYTHON_COMPAT_H */
