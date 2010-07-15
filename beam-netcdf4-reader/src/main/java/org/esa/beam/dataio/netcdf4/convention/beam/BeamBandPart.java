/*
 * $Id$
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.beam.dataio.netcdf4.convention.beam;

import org.esa.beam.dataio.netcdf4.convention.Profile;
import org.esa.beam.dataio.netcdf4.convention.ProfilePart;
import org.esa.beam.dataio.netcdf4.convention.cf.CfBandPart;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.List;

import static org.esa.beam.dataio.netcdf4.Nc4ReaderUtils.*;

public class BeamBandPart extends ProfilePart {

    public static final String BANDWIDTH = "bandwidth";
    public static final String WAVELENGTH = "wavelength";
    public static final String VALID_PIXEL_EXPRESSION = "valid_pixel_expression";

    @Override
    public void read(Profile profile, Product p) throws IOException {
        final List<Variable> variables = profile.getFileInfo().getGlobalVariables();
        for (Variable variable : variables) {
            final List<Dimension> dimensions = variable.getDimensions();
            if (dimensions.size() != 2) {
                continue;
            }
            final int yDimIndex = 0;
            final int xDimIndex = 1;
            if (dimensions.get(yDimIndex).getLength() == p.getSceneRasterHeight()
                && dimensions.get(xDimIndex).getLength() == p.getSceneRasterWidth()) {
                final int rasterDataType = getRasterDataType(variable.getDataType(), variable.isUnsigned());
                final Band band = p.addBand(variable.getName(), rasterDataType);
                applyAttributes(band, variable);
            }
        }
    }

    @Override
    public void define(Profile ctx, Product p, NetcdfFileWriteable ncFile) throws IOException {
        final Band[] bands = p.getBands();
        final List<Dimension> dimensions = ncFile.getRootGroup().getDimensions();
        for (Band band : bands) {
            final DataType ncDataType = CfBandPart.getNcDataType(band);
            final Variable variable = ncFile.addVariable(band.getName(), ncDataType, dimensions);
            addAttributes(variable, band);
        }
    }

    public static void applyAttributes(Band band, Variable variable) {
        CfBandPart.applyAttributes(band, variable);

        final Product product = band.getProduct();
        final MetadataElement dsd = product.getMetadataRoot().getElement("DSD");
        final MetadataElement bandElem = dsd.getElement(band.getName());

        // todo se -- Log10 Scaling
        // todo se -- units for bandwidth and wavelength

        final String attribNameBandwidth = BANDWIDTH;
        if (bandElem.containsAttribute(attribNameBandwidth)) {
            band.setSpectralBandwidth((float) bandElem.getAttributeDouble(attribNameBandwidth));
        }
        final String attribNameWavelength = WAVELENGTH;
        if (bandElem.containsAttribute(attribNameWavelength)) {
            band.setSpectralWavelength((float) bandElem.getAttributeDouble(attribNameWavelength));
        }
        final String attribNameValidPixelExpression = VALID_PIXEL_EXPRESSION;
        if (bandElem.containsAttribute(attribNameValidPixelExpression)) {
            band.setValidPixelExpression(bandElem.getAttributeString(attribNameValidPixelExpression));
        }
    }

    public static void addAttributes(Variable variable, Band band) {
        CfBandPart.addAttributes(variable, band);

        // todo se -- Log10 Scaling
        // todo se -- units for bandwidth and wavelength

        final float spectralBandwidth = band.getSpectralBandwidth();
        if (spectralBandwidth > 0) {
            variable.addAttribute(new Attribute(BANDWIDTH, spectralBandwidth));
        }
        final float spectralWavelength = band.getSpectralWavelength();
        if (spectralWavelength > 0) {
            variable.addAttribute(new Attribute(WAVELENGTH, spectralWavelength));
        }
        final String validPixelExpression = band.getValidPixelExpression();
        if (validPixelExpression != null && validPixelExpression.trim().length() > 0) {
            variable.addAttribute(new Attribute(VALID_PIXEL_EXPRESSION, validPixelExpression));
        }
    }

}
