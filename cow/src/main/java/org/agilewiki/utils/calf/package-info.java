/**
 * A basic implementation of a small Copy-On-Write database.
 * There is no logging of transactions and all data is kept in a
 * single block which is alternately written to two locations.
 */
package org.agilewiki.utils.calf;