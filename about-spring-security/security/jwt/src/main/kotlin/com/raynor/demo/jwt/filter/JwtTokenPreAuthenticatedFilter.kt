package com.raynor.demo.jwt.filter

//class JwtTokenPreAuthenticatedFilter(
//    private val jwtHelper: JwtHelper,
//) : AbstractPreAuthenticatedProcessingFilter() {
//
//    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any? {
//        val token = request.getHeader(HttpHeaders.AUTHORIZATION)?.substringAfter("Bearer ")
//            ?: return null
//
//        return if (jwtHelper.isValidToken(token)) {
//            CustomAuthenticationToken(token)
//        } else {
//            null
//        }
//    }
//
//    override fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any? {
//        return null
//    }
//}